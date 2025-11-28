package johnsmith.enchantingoverhauled.client.render.entity.model;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TomeModel extends BookModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanting_book"), "main");

    private final ModelPart buckle;

    public TomeModel(ModelPart root) {
        super(root);
        this.buckle = root.getChild("right_lid").getChild("buckle");
    }

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

        // Buckle definition
        CubeListBuilder buckleBuilder = CubeListBuilder.create()
                .texOffs(28, 4)
                .addBox(0.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F));

        rightLid.addOrReplaceChild("buckle", buckleBuilder,
                PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, (float)(Math.PI / 2F), 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(@NotNull BookModel.State state) {
        super.setupAnim(state);

        // 1. Access the 'open' field from the State record (0.0 = closed, 1.0 = open)
        float openAmount = state.open();

        // 2. Animate the buckle
        // When openAmount is 0, rot is PI/2. When open is 1, rot is -PI/2 (flips over).
        this.buckle.yRot = (float)(Math.PI / 2F) - (openAmount * (float)Math.PI);
    }
}