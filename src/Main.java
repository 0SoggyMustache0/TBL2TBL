import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/***
 * TBL 5 to TBL 4 Converter
 * @author George Kazanjian
 */
public class Main {

	public static String resultPath;
	public static File tempFile = new File("temp.json");

	public static void main(String[] args) {
		String sourcePath = args[0];
		resultPath = args[1];

		File file = new File(sourcePath);

		try {
			String fileZip = file.getAbsolutePath();

			byte[] buffer = new byte[1024];
			ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
			ZipEntry zipEntry = zis.getNextEntry();

			while (zipEntry != null) {
				if (zipEntry.getName().endsWith(".json")) {
					FileOutputStream fos = new FileOutputStream(tempFile);
					int len;
					while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
					fos.close();
					doStuff();
					return;
				} else {
					zipEntry = zis.getNextEntry();
				}
			}

			zis.closeEntry();
			zis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void doStuff() throws IOException {
		JsonObject jo = (JsonObject) new JsonParser().parse(new FileReader(tempFile));

		TabulaModel model = new TabulaModel();

		model.modelName = jo.get("name").getAsString();
		model.texHeight = jo.get("texHeight").getAsInt();
		model.texWidth = jo.get("texWidth").getAsInt();

		JsonArray array = jo.get("parts").getAsJsonArray();

		for (int i = 0; i < array.size(); i++) {
			JsonObject object = array.get(i).getAsJsonObject();
			ModelPart part = createPartFromJson(object);
			recursiveLoadChildren(object, part);
			model.parts.add(part);
		}

		tempFile.delete();

		JsonObject nModel = new JsonObject();
		nModel.addProperty("authorName", "UrMom");
		nModel.add("metaData", new JsonArray());
		nModel.addProperty("modelName", model.modelName);
		nModel.addProperty("projVersion", 4);
		nModel.addProperty("textureWidth", model.texWidth);
		nModel.addProperty("textureHeight", model.texHeight);
		JsonArray scale = new JsonArray();
		scale.add(1.0);
		scale.add(1.0);
		scale.add(1.0);
		nModel.add("scale", scale);
		nModel.add("cubeGroups", new JsonArray());

		JsonArray cubes = new JsonArray();
		for (ModelPart part : model.parts) {
			JsonObject ob = new JsonObject();
			addProperties(ob, part);
			recursiveExportChildren(ob, part);
			cubes.add(ob);
		}

		nModel.add("cubes", cubes);
		nModel.addProperty("cubeCount", cubes.size());

		File f = new File(resultPath);
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
		ZipEntry e = new ZipEntry("model.json");
		out.putNextEntry(e);

		byte[] data = nModel.toString().getBytes();
		out.write(data, 0, data.length);
		out.closeEntry();

		out.close();
	}

	public static void recursiveExportChildren(JsonObject ob, ModelPart parent){
		JsonArray arr = new JsonArray();
		for (ModelPart child : parent.children) {
			JsonObject pp = new JsonObject();
			addProperties(pp, child);
			pp.addProperty("parentIdentifier", parent.identifier);
			recursiveExportChildren(pp, child);
			arr.add(pp);
		}
		ob.add("children", arr);
	}

	public static void addProperties(JsonObject ob, ModelPart part){
		ob.addProperty("name", part.name);
		ob.add("dimensions", toJsonArray(part.dimensions));
		ob.add("position", toJsonArray(part.position));
		ob.add("offset", toJsonArray(part.offset));
		ob.add("rotation", toJsonArray(part.rotation));
		ob.add("scale", toJsonArray(new float[]{1.0F,1.0F,1.0F}));
		ob.add("txOffset", toJsonArray(part.textureOffset));
		ob.addProperty("mcScale", 0.0F);
		ob.addProperty("opacity", 100.0F);
		ob.addProperty("hidden", false);
		ob.add("metaData", new JsonArray());
		ob.addProperty("identifier", part.identifier);
		ob.addProperty("txMirror", part.mirror);
	}

	public static JsonArray toJsonArray(int[] a){
		JsonArray json = new JsonArray();
		Arrays.stream(a).forEach(json::add);
		return json;
	}

	public static JsonArray toJsonArray(float[] a){
		JsonArray json = new JsonArray();
		for (float v : a) {
			json.add(v);
		}
		return json;
	}

	public static void recursiveLoadChildren(JsonObject jsonObject, ModelPart parent) {
		JsonArray children = jsonObject.get("children").getAsJsonArray();
		if (children.size() <= 0) return;

		for (int i = 0; i < children.size(); i++) {
			JsonObject object = children.get(i).getAsJsonObject();

			ModelPart part = createPartFromJson(object);
			part.parentIdentifier = parent.identifier;
			parent.children.add(part);

			recursiveLoadChildren(object, part);
		}
	}

	public static ModelPart createPartFromJson(JsonObject object) {
		ModelPart part = new ModelPart();
		part.name = object.get("name").getAsString();
		part.identifier = object.get("identifier").getAsString();
		part.mirror = object.get("mirror").getAsBoolean();
		JsonObject boxes = object.get("boxes").getAsJsonArray().get(0).getAsJsonObject();
		part.dimensions[0] = boxes.get("dimX").getAsFloat();
		part.dimensions[1] = boxes.get("dimY").getAsFloat();
		part.dimensions[2] = boxes.get("dimZ").getAsFloat();
		part.offset[0] = boxes.get("posX").getAsInt();
		part.offset[1] = boxes.get("posY").getAsInt();
		part.offset[2] = boxes.get("posZ").getAsInt();
		part.position[0] = object.get("rotPX").getAsFloat();
		part.position[1] = object.get("rotPY").getAsFloat();
		part.position[2] = object.get("rotPZ").getAsFloat();
		part.rotation[0] = object.get("rotAX").getAsFloat();
		part.rotation[1] = object.get("rotAY").getAsFloat();
		part.rotation[2] = object.get("rotAZ").getAsFloat();
		part.textureOffset[0] = object.get("texOffX").getAsInt();
		part.textureOffset[1] = object.get("texOffY").getAsInt();
		return part;
	}

}
