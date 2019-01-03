package com.tommytony.war.spells;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.spells.passive.PassiveListener;
import com.nisovin.magicspells.spells.passive.PassiveTrigger;
import com.nisovin.magicspells.util.OverridePriority;
import com.tommytony.war.event.WarPlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class WarDeathListener extends PassiveListener {

    List<PassiveSpell> spells = new ArrayList<>();

    @Override
    public void registerSpell(PassiveSpell passiveSpell, PassiveTrigger passiveTrigger, String s) {
        spells.add(passiveSpell);
    }

    @OverridePriority
    @EventHandler
    public void onWarPlayerDeath(WarPlayerDeathEvent event) {
        Player victim = event.getVictim();
        Spellbook spellbook = MagicSpells.getSpellbook(victim);
        spells.stream().filter(spellbook::hasSpell).forEachOrdered(spell -> spell.activate(victim));
    }
}
