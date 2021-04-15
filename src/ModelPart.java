import java.util.ArrayList;
import java.util.List;

public class ModelPart {

	public String identifier = "", parentIdentifier = "";
	public String name = "NO NAME";
	public float[] dimensions = new float[3];
	public float[] position = new float[3];
	public int[] offset = new int[3];
	public int[] textureOffset = new int[2];
	public float[] rotation = new float[3];
	public boolean mirror;
	public List<ModelPart> children = new ArrayList<>();

}
