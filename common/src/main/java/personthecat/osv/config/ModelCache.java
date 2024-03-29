package personthecat.osv.config;

import lombok.EqualsAndHashCode;
import personthecat.osv.preset.data.ModelSettings;

import java.io.Serializable;

@EqualsAndHashCode
public class ModelCache implements Serializable {
    public final ModelSettings.Type modelType = Cfg.modelType();
    public final double overlayScale = Cfg.overlayScale();
    public final boolean shadeModifier = Cfg.shadeModifier();
    public final boolean denseOres = Cfg.denseOres();
}
