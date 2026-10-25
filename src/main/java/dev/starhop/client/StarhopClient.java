package dev.starhop.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.starhop.Starhop;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side platforming moves:
 *  - Triple jump: chain jumps shortly after landing while moving.
 *  - Spin: press the Spin key once per jump to hover briefly in mid-air.
 *
 * All values are tuned so a normal triple jump stays under 3 blocks, which means
 * no fall damage. Raise them and you will start taking fall damage.
 */
public class StarhopClient implements ClientModInitializer {
	// Jump "initial velocities" (vanilla jump is 0.42).
	private static final double JUMP_STAGE_2 = 0.52;
	private static final double JUMP_STAGE_3 = 0.66;
	// Max ticks between landing and the next jump for it to count as a chain.
	// Holding jump in vanilla auto-jumps every 10 ticks, so this must be above 10.
	private static final int CHAIN_WINDOW_TICKS = 12;
	private static final double MIN_CHAIN_SPEED = 0.08;
	private static final int SPIN_TICKS = 12;
	private static final double SPIN_MAX_FALL_SPEED = -0.08;

	private static final KeyMapping.Category CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Starhop.MOD_ID, "starhop"));

	private static KeyMapping spinKey;

	private boolean wasOnGround = true;
	private int ticksSinceLanding = 100;
	private int jumpStage = 0;
	private boolean spunThisJump = false;
	private int spinTicksLeft = 0;

	@Override
	public void onInitializeClient() {
		spinKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.starhop.spin", InputConstants.KEY_R, CATEGORY)
		);
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null || mc.isPaused()) {
			return;
		}

		// Don't interfere with flying, swimming, climbing, riding or spectating.
		if (player.isSpectator() || player.getAbilities().flying || player.isInWater()
			|| player.isInLava() || player.onClimbable() || player.isPassenger()) {
			resetState(player.onGround());
			// Drain any queued spin presses so they don't fire later.
			while (spinKey.consumeClick()) { /* discard */ }
			return;
		}

		boolean onGround = player.onGround();
		Vec3 velocity = player.getDeltaMovement();

		// --- Landing ---
		if (onGround && !wasOnGround) {
			ticksSinceLanding = 0;
			spunThisJump = false;
			spinTicksLeft = 0;
		} else if (onGround) {
			ticksSinceLanding = Math.min(ticksSinceLanding + 1, 100);
		}

		// --- Jump detection (left the ground while moving upward) ---
		if (wasOnGround && !onGround && velocity.y > 0.1) {
			boolean chained = jumpStage > 0
				&& ticksSinceLanding <= CHAIN_WINDOW_TICKS
				&& velocity.horizontalDistance() > MIN_CHAIN_SPEED;
			jumpStage = chained ? (jumpStage % 3) + 1 : 1;

			if (jumpStage >= 2) {
				double jumpVelocity = (jumpStage == 3) ? JUMP_STAGE_3 : JUMP_STAGE_2;
				// The vanilla jump already moved us this tick; set next tick's upward speed
				// as if we had started at jumpVelocity (gravity 0.08, drag 0.98).
				double nextTickY = (jumpVelocity - 0.08) * 0.98;
				player.setDeltaMovement(velocity.x, nextTickY, velocity.z);
				puff(player, jumpStage == 3 ? 10 : 5);
			}
		}

		// --- Spin ---
		boolean spinRequested = false;
		while (spinKey.consumeClick()) {
			spinRequested = true;
		}
		if (spinRequested && !onGround && !spunThisJump) {
			spunThisJump = true;
			spinTicksLeft = SPIN_TICKS;
			Vec3 v = player.getDeltaMovement();
			player.setDeltaMovement(v.x, Math.max(v.y, 0.2), v.z);
		}

		if (spinTicksLeft > 0 && !onGround) {
			spinTicksLeft--;
			Vec3 v = player.getDeltaMovement();
			if (v.y < SPIN_MAX_FALL_SPEED) {
				player.setDeltaMovement(v.x, SPIN_MAX_FALL_SPEED, v.z);
			}
			spinParticles(player, spinTicksLeft);
		}

		wasOnGround = onGround;
	}

	private void resetState(boolean onGround) {
		wasOnGround = onGround;
		jumpStage = 0;
		spunThisJump = false;
		spinTicksLeft = 0;
	}

	private void puff(LocalPlayer player, int count) {
		for (int i = 0; i < count; i++) {
			double dx = (player.getRandom().nextDouble() - 0.5) * 0.6;
			double dz = (player.getRandom().nextDouble() - 0.5) * 0.6;
			player.level().addParticle(ParticleTypes.CLOUD,
				player.getX() + dx, player.getY() + 0.1, player.getZ() + dz, 0.0, 0.02, 0.0);
		}
	}

	private void spinParticles(LocalPlayer player, int ticksLeft) {
		double angle = ticksLeft * 0.9;
		for (int i = 0; i < 3; i++) {
			double a = angle + i * (Math.PI * 2.0 / 3.0);
			player.level().addParticle(ParticleTypes.END_ROD,
				player.getX() + Math.cos(a) * 0.9, player.getY() + 1.0, player.getZ() + Math.sin(a) * 0.9,
				0.0, 0.0, 0.0);
		}
	}
}
