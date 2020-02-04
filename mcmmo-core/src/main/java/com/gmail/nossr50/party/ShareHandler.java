package com.gmail.nossr50.party;

import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.experience.XPGainSource;
import com.gmail.nossr50.datatypes.party.ItemShareType;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.party.ShareMode;
import com.gmail.nossr50.datatypes.player.BukkitMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class ShareHandler {
    private final mcMMO pluginRef;
    private Party party;

    public ShareHandler(mcMMO pluginRef, Party party) {
        this.pluginRef = pluginRef;
        this.party = party;
    }

    /**
     * Distribute Xp amongst party members.
     *
     * @param xp               Xp without party sharing
     * @param mcMMOPlayer      Player initiating the Xp gain
     * @param primarySkillType Skill being used
     * @return True is the xp has been shared
     */
    public boolean handleXpShare(double xp, BukkitMMOPlayer mcMMOPlayer, PrimarySkillType primarySkillType, XPGainReason xpGainReason) {
        if (party.getXpShareMode() != ShareMode.EQUAL) {
            return false;
        }

        List<Player> nearMembers = pluginRef.getPartyManager().getNearVisibleMembers(mcMMOPlayer);

        if (nearMembers.isEmpty()) {
            return false;
        }

        nearMembers.add(mcMMOPlayer.getNative());

        int partySize = nearMembers.size();
        double shareBonus = Math.min(pluginRef.getPartyXPShareSettings().getPartyShareXPBonusBase()
                        + (partySize * pluginRef.getPartyXPShareSettings().getPartyShareBonusIncrease()),
                pluginRef.getPartyXPShareSettings().getPartyShareBonusCap());
        double splitXp = (xp / partySize * shareBonus);

        for (Player member : nearMembers) {
            //Profile not loaded
            if (pluginRef.getUserManager().getPlayer(member) == null) {
                continue;
            }

            pluginRef.getUserManager().getPlayer(member).beginUnsharedXpGain(primarySkillType, splitXp, xpGainReason, XPGainSource.PARTY_MEMBERS);
        }

        return true;
    }

    /**
     * Distribute Items amongst party members.
     *
     * @param drop        Item that will get shared
     * @param mcMMOPlayer Player who picked up the item
     * @return True if the item has been shared
     */
    public boolean handleItemShare(Item drop, BukkitMMOPlayer mcMMOPlayer) {
        ItemStack itemStack = drop.getItemStack();
        ItemShareType dropType = getShareType(itemStack);

        if (dropType == null) {
            return false;
        }

        if (!party.sharingDrops(dropType)) {
            return false;
        }

        ShareMode shareMode = party.getItemShareMode();

        if (shareMode == ShareMode.NONE) {
            return false;
        }

        List<Player> nearMembers = pluginRef.getPartyManager().getNearMembers(mcMMOPlayer);

        if (nearMembers.isEmpty()) {
            return false;
        }

        Player winningPlayer = null;
        ItemStack newStack = itemStack.clone();

        nearMembers.add(mcMMOPlayer.getNative());
        int partySize = nearMembers.size();

        drop.remove();
        newStack.setAmount(1);

        switch (shareMode) {
            case EQUAL:
                int itemWeight = getItemWeight(itemStack.getType());

                for (int i = 0; i < itemStack.getAmount(); i++) {
                    int highestRoll = 0;

                    for (Player member : nearMembers) {
                        BukkitMMOPlayer mcMMOMember = pluginRef.getUserManager().getPlayer(member);

                        //Profile not loaded
                        if (pluginRef.getUserManager().getPlayer(member) == null) {
                            continue;
                        }

                        int itemShareModifier = mcMMOMember.getItemShareModifier();
                        int diceRoll = pluginRef.getMiscTools().getRandom().nextInt(itemShareModifier);

                        if (diceRoll <= highestRoll) {
                            mcMMOMember.setItemShareModifier(itemShareModifier + itemWeight);
                            continue;
                        }

                        highestRoll = diceRoll;

                        if (winningPlayer != null) {
                            BukkitMMOPlayer mcMMOWinning = pluginRef.getUserManager().getPlayer(winningPlayer);
                            mcMMOWinning.setItemShareModifier(mcMMOWinning.getItemShareModifier() + itemWeight);
                        }

                        winningPlayer = member;
                    }

                    BukkitMMOPlayer mcMMOTarget = pluginRef.getUserManager().getPlayer(winningPlayer);
                    mcMMOTarget.setItemShareModifier(mcMMOTarget.getItemShareModifier() - itemWeight);
                    awardDrop(winningPlayer, newStack);
                }

                return true;

            case RANDOM:
                for (int i = 0; i < itemStack.getAmount(); i++) {
                    winningPlayer = nearMembers.get(pluginRef.getMiscTools().getRandom().nextInt(partySize));
                    awardDrop(winningPlayer, newStack);
                }

                return true;

            default:
                return false;
        }
    }

    public ItemShareType getShareType(ItemStack itemStack) {
        if (pluginRef.getItemTools().isMobDrop(itemStack)) {
            return ItemShareType.LOOT;
        } else if (pluginRef.getItemTools().isMiningDrop(itemStack)) {
            return ItemShareType.MINING;
        } else if (pluginRef.getItemTools().isHerbalismDrop(itemStack)) {
            return ItemShareType.HERBALISM;
        } else if (pluginRef.getItemTools().isWoodcuttingDrop(itemStack)) {
            return ItemShareType.WOODCUTTING;
        } else if (pluginRef.getConfigManager().getConfigParty().getPartyItemShare().getItemShareMap().get(itemStack.getType()) != null) {
            return ItemShareType.MISC;
        }

        return null;
    }

    private int getItemWeight(Material material) {
        if (pluginRef.getConfigManager().getConfigParty().getPartyItemShare().getItemShareMap().get(material) == null)
            return 5;
        else
            return pluginRef.getConfigManager().getConfigParty().getPartyItemShare().getItemShareMap().get(material);
    }

    public XPGainReason getSharedXpGainReason(XPGainReason xpGainReason) {
        if (xpGainReason == XPGainReason.PVE) {
            return XPGainReason.SHARED_PVE;
        } else if (xpGainReason == XPGainReason.PVP) {
            return XPGainReason.SHARED_PVP;
        } else {
            return xpGainReason;
        }
    }

    private void awardDrop(Player winningPlayer, ItemStack drop) {
        if (winningPlayer.getInventory().addItem(drop).size() != 0) {
            winningPlayer.getWorld().dropItem(winningPlayer.getLocation(), drop);
        }

        winningPlayer.updateInventory();
    }
}
