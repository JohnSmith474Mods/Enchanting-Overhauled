package johnsmith.enchantingoverhauled.client.render.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * A custom model class for the Enchanted Tome/Enchanting Table book.
 * <p>
 * This class extends the vanilla {@link BookModel} to add a decorative buckle component
 * to the book's cover. It is used to render the book on the Enchanting Table block entity.
 */
public class TomeModel extends BookModel {

    /**
     * The unique layer location for this model, registered under the mod's namespace.
     */
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(Constants.MOD_ID, "enchanting_book"), "main");

    private final ModelPart root;

    private final ModelPart buckle;

    /**
     * Constructs a new TomeModel.
     *
     * @param root The root model part containing all the book components.
     */
    public TomeModel(ModelPart root) {
        super(root);
        this.root = root;
        this.buckle = root.getChild("right_lid").getChild("buckle");
    }

    /**
     * Defines the geometry for the custom book model.
     * <p>
     * This definition recreates the vanilla book structure (lids, seam, pages) and adds
     * a "buckle" child part to the "right_lid". The buckle rotates with the lid.
     *
     * @return A LayerDefinition containing the mesh and texture mapping for the model.
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition rightLid = root.addOrReplaceChild("right_lid", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, 1.0F));
        root.addOrReplaceChild("left_lid", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, -1.0F));
        root.addOrReplaceChild("seam", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F), PartPose.rotation(0.0F, ((float)Math.PI / 2F), 0.0F));
        root.addOrReplaceChild("left_pages", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("right_pages", CubeListBuilder.create().texOffs(12, 10).addBox(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);

        CubeListBuilder pageBuilder = CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
        root.addOrReplaceChild("flip_page1", pageBuilder, PartPose.ZERO);
        root.addOrReplaceChild("flip_page2", pageBuilder, PartPose.ZERO);

        CubeListBuilder buckleBuilder = CubeListBuilder.create()
                .texOffs(28, 4)
                .addBox(
                        0.0F, -1.0F, 0.0F,
                        2.0F, 2.0F, 0.0F,
                        new CubeDeformation(0.0F)
                );

        rightLid.addOrReplaceChild("buckle", buckleBuilder,
                PartPose.offsetAndRotation(
                        6.0F, 0.0F, 0.0F,
                        0.0F, (float)(Math.PI / 2F), 0.0F
                )
        );

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    /**
     * Renders the model to the given buffer.
     * <p>
     * This method delegates rendering to the root part, ensuring all child parts (including the buckle) are drawn.
     *
     * @param poseStack     The current matrix stack.
     * @param buffer        The vertex consumer to draw to.
     * @param packedLight   The packed lightmap coordinates.
     * @param packedOverlay The packed overlay coordinates.
     * @param red           The red color component (0.0-1.0).
     * @param green         The green color component (0.0-1.0).
     * @param blue          The blue color component (0.0-1.0).
     * @param alpha         The alpha transparency component (0.0-1.0).
     */
    @Override
    public void renderToBuffer(
            @NotNull PoseStack poseStack,
            @NotNull VertexConsumer buffer,
            int packedLight, int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    /**
     * Updates the model's animations based on the current time and page state.
     * <p>
     * This method calls the super method to handle standard book animations (page flipping,
     * cover opening) and then manually updates the buckle's rotation to ensure it stays
     * aligned with the book's movement logic.
     *
     * @param time          The current game time in ticks.
     * @param rightPageFlip The flip amount for the right page.
     * @param leftPageFlip  The flip amount for the left page.
     * @param pageFlip      The interpolated open amount of the book (0.0 = closed, 1.0 = open).
     */
    @Override
    public void setupAnim(float time, float rightPageFlip, float leftPageFlip, float pageFlip) {
        super.setupAnim(time, rightPageFlip, leftPageFlip, pageFlip);
        this.buckle.yRot = (float)(Math.PI / 2F) - (pageFlip * (float)Math.PI);
    }
}