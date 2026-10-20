package johnsmith.enchantingoverhauled.client.particle;

import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


public class SgaRuneParticle extends TextureSheetParticle {

    private static final ThreadLocal<Integer> PENDING_TINT_COLOR = new ThreadLocal<>();
    private static final ThreadLocal<Integer> PENDING_LIFETIME = new ThreadLocal<>();

    private final SpriteSet sprites;

    protected SgaRuneParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        Integer customLifetime = PENDING_LIFETIME.get();
        if (customLifetime != null) {
            this.lifetime = customLifetime;
            this.hasPhysics = false;
            this.friction = 1.0F;
        } else {
            this.lifetime = (int) (Math.random() * 20.0D) + 20;
        }
        this.gravity = 0.0F;
        this.pickSprite(sprites);
        this.quadSize *= 0.5F;

        int resolvedColor = resolveTintColor(level, x, y, z);
        applyLightTint(resolvedColor);
    }

    public static void spawnTinted(Level level, double x, double y, double z, double vx, double vy, double vz, int rgbColor) {
        if (!level.isClientSide()) {
            return;
        }
        PENDING_TINT_COLOR.set(rgbColor & 0xFFFFFF);
        try {
            level.addParticle(SimpleParticleTypes.SGA_RUNE.type(), x, y, z, vx, vy, vz);
        } finally {
            PENDING_TINT_COLOR.remove();
        }
    }

    public static void spawnTransferRune(Level level, Vec3 from, Vec3 to, int rgbColor) {
        if (!level.isClientSide()) {
            return;
        }
        Vec3 delta = to.subtract(from);
        double dist = delta.length();
        int travelTicks = Mth.clamp((int) (dist * 5.5D), 14, 42);
        double vx = delta.x / (double) travelTicks;
        double vy = (delta.y / (double) travelTicks) + 0.012D;
        double vz = delta.z / (double) travelTicks;

        PENDING_TINT_COLOR.set(rgbColor & 0xFFFFFF);
        PENDING_LIFETIME.set(travelTicks);
        try {
            level.addParticle(SimpleParticleTypes.SGA_RUNE.type(), from.x, from.y, from.z, vx, vy, vz);
        } finally {
            PENDING_TINT_COLOR.remove();
            PENDING_LIFETIME.remove();
        }
    }

    private static int resolveTintColor(ClientLevel level, double x, double y, double z) {
        Integer explicit = PENDING_TINT_COLOR.get();
        if (explicit != null) {
            return explicit;
        }
        return 0xFFFFFF;
    }

    private void applyLightTint(int rgb) {
        int cleanRgb = rgb & 0xFFFFFF;
        if (cleanRgb == 0xFFFFFF || cleanRgb == 0) {
            this.rCol = 0.92F;
            this.gCol = 0.92F;
            this.bCol = 0.92F;
            return;
        }
        float sr = ((cleanRgb >> 16) & 0xFF) / 255.0F;
        float sg = ((cleanRgb >> 8) & 0xFF) / 255.0F;
        float sb = (cleanRgb & 0xFF) / 255.0F;

        this.rCol = Mth.lerp(0.65F, 0.95F, sr);
        this.gCol = Mth.lerp(0.65F, 0.95F, sg);
        this.bCol = Mth.lerp(0.65F, 0.95F, sb);
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = 1.0F - ((float) this.age / (float) this.lifetime);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SgaRuneParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}