package com.tranfode.processor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.tranfode.Constants.BinderConstants;
import com.tranfode.Constants.CloudFileConstants;
import com.tranfode.domain.FileItContext;
import com.tranfode.util.CloudFilesOperationUtil;
import com.tranfode.util.CloudPropertiesReader;
import com.tranfode.util.CloudStorageConfig;
import com.tranfode.util.FileInfoPropertyReader;
import com.tranfode.util.FileItException;

public class ContentProcessor {
	FileItContext fileItContext;
	List<String> paths = new ArrayList<String>();
	private static ContentProcessor INSTANCE;
	static CloudFilesOperationUtil cloudFilesOperationUtil = new CloudFilesOperationUtil();

	public static synchronized ContentProcessor getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new ContentProcessor();
		}
		return INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public int processContentImage(String bookName, InputStream inputFile, String path, String type, String fileName,
			int pagecounter) throws FileItException {
		JSONObject oJsonObject = new JSONObject();
		PDDocument document = null;
		JSONArray oJsonArray = new JSONArray();
		System.setProperty("org.apache.pdfbox.baseParser.pushBackSize", "999000");
		try {
			if (type.equalsIgnoreCase("docx")) {
				XWPFDocument document1 = new XWPFDocument(OPCPackage.open(inputFile));
				PdfOptions options = PdfOptions.create();
				OutputStream out = new FileOutputStream(new File("D://test.pdf"));
				PdfConverter.getInstance().convert(document1, out, options);
				BufferedImage bufferedImage = null;
				File newFIle = new File("./test.pdf");
				document = PDDocument.load(newFIle);
				newFIle.delete();
				List<PDPage> pages = document.getDocumentCatalog().getAllPages();
				for (PDPage page : pages) {
					pagecounter++;
					bufferedImage = page.convertToImage();
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(bufferedImage, "gif", os);
					InputStream is = new ByteArrayInputStream(os.toByteArray());
					cloudFilesOperationUtil.fIleUploaded(path + pagecounter + BinderConstants.IMG_EXTENSION, is,
							CloudFileConstants.IMGFILETYPE);
					oJsonArray.add(path + pagecounter + BinderConstants.IMG_EXTENSION);
					is.close();
					os.close();
				}
				oJsonObject.put("Success", "File Uploaded Successfully");
			} else if (type.equalsIgnoreCase("pptx")) {
				XMLSlideShow ppt = new XMLSlideShow(inputFile);
				XSLFSlide[] slides = ppt.getSlides();
				Dimension pgsize = ppt.getPageSize();
				for (int i = 0; i < slides.length; i++) {
					pagecounter++;
					BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.SCALE_SMOOTH);
					Graphics2D graphics = img.createGraphics();
					// clear the drawing area
					graphics.setPaint(Color.white);
					graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
					// render
					slides[i].draw(graphics);
					// save the output
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					javax.imageio.ImageIO.setUseCache(false);
					javax.imageio.ImageIO.write(img, "JPG", os);
					InputStream is = new ByteArrayInputStream(os.toByteArray());
					cloudFilesOperationUtil.fIleUploaded(path + pagecounter + BinderConstants.IMG_EXTENSION, is,
							CloudFileConstants.IMGFILETYPE);
					os.close();
					is.close();
				}
				oJsonObject.put("Success", "File Uploaded Successfully");
			} else {
				// long lStartTime = System.currentTimeMillis();
				BufferedImage bufferedImage = null;
				document = PDDocument.load(inputFile);
				List<PDPage> pages = document.getDocumentCatalog().getAllPages();
				for (PDPage page : pages) {
					pagecounter++;
					bufferedImage = page.convertToImage();
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					javax.imageio.ImageIO.write(bufferedImage, "JPG", os);
					InputStream is = new ByteArrayInputStream(os.toByteArray());
					cloudFilesOperationUtil.fIleUploaded(path + pagecounter + BinderConstants.IMG_EXTENSION, is,
							CloudFileConstants.IMGFILETYPE);
					is.close();
					oJsonArray.add(path + pagecounter + BinderConstants.IMG_EXTENSION);
				}
				oJsonObject.put("Success", "File Uploaded Successfully");
				/*
				 * long lEndTime = System.currentTimeMillis(); long output = lEndTime -
				 * lStartTime; System.out.println("Timing in ImageCOnersion in  MilliSeconds"+
				 * output);
				 */
			}
			/*
			 * long lStartTime = System.currentTimeMillis(); InputStream is = new
			 * ByteArrayInputStream(oJsonArray.toJSONString().getBytes());
			 * cloudFilesOperationUtil.fIleUploaded("files/" + fileName.subSequence(0,
			 * fileName.lastIndexOf('.')) + ".JSON",is,CloudFileConstants.JSONFILETYPE);
			 * long lEndTime = System.currentTimeMillis(); long output = lEndTime -
			 * lStartTime; System.out.println("Timing in individualJSON"+ output);
			 */ } catch (IOException e) {
			throw new FileItException(e.getMessage());
		} catch (Exception e) {
			throw new FileItException(e.getMessage());
		}
		return pagecounter;
	}

	public static String createDyanmicImagePath(int i, String bookName, String extension) {
		boolean isDirectory = false;
		String fullContentDirectory = null;
		String absoluteImgPath = null;

		String counter = String.valueOf(i);
		String staticPath = FileInfoPropertyReader.getInstance().getString("doc.static.path");
		fullContentDirectory = staticPath.concat("\\" + bookName + "\\Images");
		java.io.File file = new File(fullContentDirectory);
		isDirectory = file.isDirectory();
		if (!isDirectory) {
			file.mkdirs();
		}
		absoluteImgPath = fullContentDirectory.concat("\\" + counter.concat(extension));
		return absoluteImgPath;
	}

	public JSONObject processContent(String bookName, String classificationName, InputStream inputFile, String path,
			String type, String fileName) throws FileItException {
		JSONObject oJsonObject = new JSONObject();
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		try {
			oCloudStorageConfig.uploadFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
					classificationName + "/" + bookName + "/Contents/" + fileName, inputFile, type);
			oJsonObject.put("Success", "File Uploaded Successfully");
			// inputFile.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new FileItException(e.getMessage());
		}
		return oJsonObject;
	}

	public void getMultipleFileDownload(String classificationame, String bookname, List<String> filename, File oFile)
			throws FileItException, IOException {
		FileOutputStream fos = new FileOutputStream(oFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		InputStream iIP;
		for (int i = 0; i < filename.size(); i++) {
			try {
				iIP = cloudFilesOperationUtil
						.getFIleInputStream(classificationame + "/" + bookname + "/Contents/" + filename.get(i));
				zos.putNextEntry(new ZipEntry(filename.get(i)));
				byte[] bytes = new byte[1024];
				int length;
				while ((length = iIP.read(bytes)) >= 0) {
					zos.write(bytes, 0, length);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new FileItException(e.getMessage());
			}
		}
		zos.closeEntry();
		zos.close();
	}

	public void getZipFile(String classificationname, String bookName, File oFile) throws FileItException, IOException {
		FileOutputStream fos = new FileOutputStream(oFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		CloudStorageConfig oCloudStorageConfig = new CloudStorageConfig();
		InputStream iIP;
		List<String> obj = oCloudStorageConfig.listBucket(CloudPropertiesReader.getInstance().getString("bucket.name"));
		String wordToSearchFor = classificationname + '/' + bookName + '/' + "Contents";
		for (String word : obj) {
			if (word.contains(wordToSearchFor))
				try {
					iIP = oCloudStorageConfig.getFile(CloudPropertiesReader.getInstance().getString("bucket.name"),
							word);
					zos.putNextEntry(new ZipEntry(word.substring(word.indexOf("Contents/") + 9, word.length())));
					byte[] bytes = new byte[1024];
					int length;
					while ((length = iIP.read(bytes)) >= 0) {
						zos.write(bytes, 0, length);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		zos.closeEntry();
		zos.close();
	}

}