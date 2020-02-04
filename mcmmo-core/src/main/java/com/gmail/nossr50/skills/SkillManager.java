package com.gmail.nossr50.skills;

import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.experience.XPGainSource;
import com.gmail.nossr50.datatypes.player.BukkitMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class SkillManager {
    protected BukkitMMOPlayer mcMMOPlayer;
    protected PrimarySkillType skill;
    protected final mcMMO pluginRef;

    public SkillManager(mcMMO pluginRef, BukkitMMOPlayer mcMMOPlayer, PrimarySkillType skill) {
        this.pluginRef = pluginRef;
        this.mcMMOPlayer = mcMMOPlayer;
        this.skill = skill;
    }

    public Player getPlayer() {
        return mcMMOPlayer.getNative();
    }

    public int getSkillLevel() {
        return mcMMOPlayer.getSkillLevel(skill);
    }

    /**
     * Applies XP to a player, provides SELF as an XpGainSource source
     *
     * @param xp           amount of XP to apply
     * @param xpGainReason the reason for the XP gain
     * @deprecated use applyXpGain(float, XPGainReason, XPGainSource)
     */
    @Deprecated
    public void applyXpGain(double xp, XPGainReason xpGainReason) {
        mcMMOPlayer.beginXpGain(skill, xp, xpGainReason, XPGainSource.SELF);
    }

    /**
     * Applies XP to a player, provides SELF as an XpGainSource source
     *
     * @param xp           amount of XP to apply
     * @param xpGainReason the reason for the XP gain
     * @deprecated use applyXpGain(float, XPGainReason, XPGainSource)
     */
    @Deprecated
    public void applyXpGain(float xp, XPGainReason xpGainReason) {
        mcMMOPlayer.beginXpGain(skill, xp, xpGainReason, XPGainSource.SELF);
    }

    /**
     * Applies XP to a player
     *
     * @param xp           amount of XP to apply
     * @param xpGainReason the reason for the XP gain
     * @param xpGainSource the source of the XP
     */
    public void applyXpGain(float xp, XPGainReason xpGainReason, XPGainSource xpGainSource) {
        mcMMOPlayer.beginXpGain(skill, xp, xpGainReason, xpGainSource);
    }

    /**
     * Applies XP to a player
     *
     * @param xp           amount of XP to apply
     * @param xpGainReason the reason for the XP gain
     * @param xpGainSource the source of the XP
     */
    public void applyXpGain(double xp, XPGainReason xpGainReason, XPGainSource xpGainSource) {
        mcMMOPlayer.beginXpGain(skill, xp, xpGainReason, xpGainSource);
    }

    public XPGainReason getXPGainReason(LivingEntity target, Entity damager) {
        return (damager instanceof Player && target instanceof Player) ? XPGainReason.PVP : XPGainReason.PVE;
    }
}
