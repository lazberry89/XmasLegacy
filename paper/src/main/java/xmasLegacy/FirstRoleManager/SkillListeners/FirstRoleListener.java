package xmasLegacy.FirstRoleManager.SkillListeners;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.FirstRoleManager.*;
import xmasLegacy.FirstRoleManager.Farmer.Farmer;
import xmasLegacy.FirstRoleManager.Gatherer.Gatherer;
import xmasLegacy.FirstRoleManager.Miner.Miner;
import xmasLegacy.FirstRoleManager.Priest.Priest;
import xmasLegacy.XmasLegacy;

@SuppressWarnings("ClassCanBeRecord")
public class FirstRoleListener implements Listener {
	private final XmasLegacy plugin;
	private final Knight knight;
	private final Rogue rogue;
	private final Archer archer;
	private final Warrior warrior;
    private final Mage mage;
	private final Priest priest;
	private final Farmer farmer;
	private final Miner miner;
	private final Gatherer gatherer;

	public FirstRoleListener(XmasLegacy plugin,
	                         Knight knight,
	                         Rogue rogue,
	                         Archer archer,
	                         Warrior warrior,
	                         Mage mage,
	                         Priest priest,
	                         Farmer farmer,
	                         Miner miner,
	                         Gatherer gatherer) {
		this.plugin = plugin;
		this.knight = knight;
		this.rogue = rogue;
		this.archer = archer;
		this.warrior = warrior;
        this.mage = mage;
		this.priest = priest;
		this.farmer = farmer;
		this.miner = miner;
		this.gatherer = gatherer;
	}

	@EventHandler
	public void onSkillUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack tool = p.getInventory().getItemInMainHand();
		ItemMeta meta = tool.getItemMeta();
		if (meta == null) return;

		NamespacedKey key = new NamespacedKey(plugin, "role_id");
		String pdc = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
		if (pdc != null) {
			if (e.getAction().isLeftClick()) {
				switch (pdc) {
					case "knight" -> {
						if (knight.getCurrentSkill(p) == null) return;
						if (knight.getCurrentSkill(p) == BasicSkills.SHARP_SWEEPING) {
							knight.useFirstSkill(p);
						} else if (knight.getCurrentSkill(p) == BasicSkills.TAUNT) {
							knight.useSecondSkill(p);
						}
					}
					case "rogue" -> {
						if (rogue.getCurrentSkill(p) == null) return;
						if (rogue.getCurrentSkill(p) == BasicSkills.DAGGER_RUSH) {
							rogue.useFirstSkill(p);
						} else if (rogue.getCurrentSkill(p) == BasicSkills.SMOKE) {
							rogue.useSecondSkill(p);
						}
					}
					case "archer" -> {
						if (archer.getCurrentSkill(p) == null) return;
						if (archer.getCurrentSkill(p) == BasicSkills.SHOCK_DART) {
							archer.useFirstSkill(p);
						} else if (archer.getCurrentSkill(p) == BasicSkills.BACK_DASH) {
							archer.useSecondSkill(p);
						}
					}
					case "warrior" -> {
						if (warrior.getCurrentSkill(p) == null) return;
						if (warrior.getCurrentSkill(p) == BasicSkills.BLOOD_FRENZY) {
							warrior.useFirstSkill(p);
						} else if (warrior.getCurrentSkill(p) == BasicSkills.TOMAHAWK) {
							warrior.useSecondSkill(p);
						}
					}
                    case "mage" -> {
                        if (mage.getCurrentSkill(p) == null) return;
                        if (mage.getCurrentSkill(p) == BasicSkills.COMPACT_INSANELY) {
                            mage.useFirstSkill(p);
                        } else if (mage.getCurrentSkill(p) == BasicSkills.GRAVITY) {
                            mage.useSecondSkill(p);
                        }
                    }
					case "priest" -> {
						if (priest.getCurrentSkill(p) == null) return;
						if (priest.getCurrentSkill(p) == BasicSkills.COMPACT_HEAL) {
							priest.useFirstSkill(p);
						} else if (priest.getCurrentSkill(p) == BasicSkills.STEROID) {
							priest.useSecondSkill(p);
						}
					}
					case "farmer" -> {
						if (farmer.getCurrentSkill(p) == null) return;
						if (farmer.getCurrentSkill(p) == BasicSkills.RADIUS_HARVEST) {
							farmer.useFirstSkill(p);
						} else if (farmer.getCurrentSkill(p) == BasicSkills.SPEED_GROWER) {
							farmer.useSecondSkill(p);
						}
					}
					case "miner" -> {
						if (miner.getCurrentSkill(p) == null) return;
						if (miner.getCurrentSkill(p) == BasicSkills.ORE_EYE) {
							miner.useFirstSkill(p);
						} else if (miner.getCurrentSkill(p) == BasicSkills.CHAIN_MINING) {
							miner.useSecondSkill(p);
						}
					}
					case "gatherer" -> {
						if (gatherer.getCurrentSkill(p) == null) return;
						if (gatherer.getCurrentSkill(p) == BasicSkills.TRUTH_EYE) {
							gatherer.useFirstSkill(p);
						} else if (gatherer.getCurrentSkill(p) == BasicSkills.ETERNAL_POSE) {
							gatherer.useSecondSkill(p);
						}
					}
				}
			}
		}
	}
	@EventHandler
	public void onSkillChange(PlayerSwapHandItemsEvent e) {
		Player p = e.getPlayer();
		ItemStack tool = p.getInventory().getItemInMainHand();
		ItemMeta meta = tool.getItemMeta();
		if (meta == null) return;

		e.setCancelled(true);

		NamespacedKey key = new NamespacedKey(plugin, "role_id");
		String pdc = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
		if (pdc != null) {
			switch (pdc) {
				case "knight" -> {
					knight.next(p);
					p.sendActionBar(ColorUtils.chat("&8&l" + knight.getCurrentSkill(p).getSkillName()));
				}
				case "rogue" -> {
					rogue.next(p);
					p.sendActionBar(ColorUtils.chat("&8&l" + rogue.getCurrentSkill(p).getSkillName()));
				}
				case "archer" -> {
					archer.next(p);
					p.sendActionBar(ColorUtils.chat("&8&l" + archer.getCurrentSkill(p).getSkillName()));
				}
				case "warrior" -> {
					warrior.next(p);
					p.sendActionBar(ColorUtils.chat("&8&l" + warrior.getCurrentSkill(p).getSkillName()));
				}
                case "mage" -> {
                    mage.next(p);
                    p.sendActionBar(ColorUtils.chat("&8&l" + mage.getCurrentSkill(p).getSkillName()));
                }
				case "priest" -> {
					priest.next(p);
					p.sendActionBar(ColorUtils.chat("&8&l" + priest.getCurrentSkill(p).getSkillName()));
				}
				case "farmer" -> {
					farmer.next(p);
					p.sendActionBar(ColorUtils.chat("&8&l" + farmer.getCurrentSkill(p).getSkillName()));
				}
				case "miner" -> {
					miner.next(p);
					p.sendActionBar(ColorUtils.chat("&8&l" + miner.getCurrentSkill(p).getSkillName()));
				}
				case "gatherer" -> {
					gatherer.next(p);
					p.sendActionBar(ColorUtils.chat("&8&l" + gatherer.getCurrentSkill(p).getSkillName()));
				}
			}
		}
	}

    //Archer skill
    @EventHandler
    public void onShockHit(ProjectileHitEvent e) {
        NamespacedKey key =  new NamespacedKey(plugin, "skill");
        String npKey = e.getEntity().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        Projectile projectile = e.getEntity();
        if (!(projectile.getShooter() instanceof Player) && !(projectile instanceof Arrow a)) return;
        if (!(npKey != null && npKey.equals("archer_arrow"))) return;

        if (e.getHitBlock() != null) {
            Location loc = e.getHitBlock().getLocation();
            loc.getWorld().strikeLightning(loc);
            return;
        }

        if (e.getHitEntity() instanceof LivingEntity victim) {
            Location loc = victim.getLocation();
	        loc.getWorld().strikeLightning(loc);
        }
    }
}
