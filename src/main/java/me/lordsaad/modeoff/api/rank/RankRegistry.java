package me.lordsaad.modeoff.api.rank;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import me.lordsaad.modeoff.ModeratorOff;
import me.lordsaad.modeoff.api.capability.DefaultModoffCapability;
import me.lordsaad.modeoff.api.capability.IModoffCapability;
import me.lordsaad.modeoff.api.capability.ModoffCapabilityProvider;
import me.lordsaad.modeoff.api.permissions.Permission;
import me.lordsaad.modeoff.api.permissions.PermissionRegistry;
import me.lordsaad.modeoff.api.rank.defaultranks.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Created by LordSaad.
 */
public class RankRegistry {
	public static RankRegistry INSTANCE = new RankRegistry();

	public HashBiMap<Integer, IRank> ranks = HashBiMap.create();
	public HashMultimap<IRank, UUID> rankMap = HashMultimap.create();

	private RankRegistry() {
		int id = 0;
		ranks.put(id++, DefaultRanks.ADMIN);
		ranks.put(id++, DefaultRanks.CONTESTANT);
		ranks.put(id++, DefaultRanks.NORMAL);
		ranks.put(id++, DefaultRanks.SPONSOR);
		ranks.put(id, DefaultRanks.JUDGE);
	}

	public boolean isAdmin(EntityPlayer player) {
		return getPermission(player).contains(PermissionRegistry.DefaultPermissions.PERMISSION_PLOT_ADMIN);
	}

	public Collection<Permission> getPermission(EntityPlayer player) {
		IModoffCapability cap = ModoffCapabilityProvider.getCap(player);
		return cap == null ? Collections.emptySet() : cap.getRank().getPermissions();
	}

	@NotNull
	public IRank getRank(EntityPlayer player) {
		IModoffCapability cap = ModoffCapabilityProvider.getCap(player);
		return cap == null ? DefaultRanks.NORMAL : cap.getRank();
	}

	@SubscribeEvent
	public void onAddCapabilities(AttachCapabilitiesEvent<Entity> e) {
		if (e.getObject() instanceof EntityPlayer) {
			ModoffCapabilityProvider provider = new ModoffCapabilityProvider(new DefaultModoffCapability());
			e.addCapability(new ResourceLocation(ModeratorOff.MOD_ID, "capabilities"), provider);
		}
	}

	@SubscribeEvent
	public void joinWorld(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayer) {

			IModoffCapability cap = ModoffCapabilityProvider.getCap(event.getEntity());
			if (cap == null) return;

			rankMap.forEach((iRank, uuid) -> {
				if (uuid.equals(event.getEntity().getUniqueID())) {
					if (cap.getRank() == iRank) return;
					cap.setRank(iRank);
					cap.dataChanged(event.getEntity());
				}
			});
		}
	}

	public static final class DefaultRanks {

		public static final RankAdmin ADMIN = new RankAdmin();
		public static final RankContestant CONTESTANT = new RankContestant();
		public static final RankJudge JUDGE = new RankJudge();
		public static final RankNormal NORMAL = new RankNormal();
		public static final RankSponsor SPONSOR = new RankSponsor();
	}
}
