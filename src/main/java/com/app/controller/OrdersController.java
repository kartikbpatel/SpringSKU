package com.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.app.model.Sku;
import com.app.repository.SkuRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

@Controller
public class OrdersController {

	@Autowired
	SkuRepository skuRepository;

	// Save the uploaded file to this folder
	private static String UPLOADED_FOLDER = "/Information Technology/Projects/Eclipse Workspace/Rohit Sir/GeschriebenTwo/src/main/resources/csv/";

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index() {
		return "redirect:/orders";
	}
	
	@RequestMapping(value = "/orders", method = RequestMethod.GET)
	public String productsList(Model model) {

		Map<String, String> vendor = new HashMap<String, String>();
		vendor.put("FO", "Fortinet");
		vendor.put("IB", "Infobox");
		vendor.put("SH", "Sophos");
		model.addAttribute("vendorslist", vendor);

		Map<String, String> skuType = new HashMap<String, String>();
		skuType.put("HOTLINE", "Hotline");
		skuType.put("HWREPLACEMENT", "HW Replacement");
		model.addAttribute("skutypelist", skuType);

		model.addAttribute("skus", skuRepository.findAll());

		return "orders";
	}

	@RequestMapping(value = "/api/download")
	public void downloadSku(HttpServletResponse response) throws IOException {
		String outputFile = UPLOADED_FOLDER + "output.csv";
		List<Sku> skus = skuRepository.findAll();
		CSVWriter writer = new CSVWriter(new FileWriter(outputFile));
		
		response.setContentType("text/csv");
		response.setHeader("Content-disposition", "attachment;filename=output.csv");

		for (Sku sku : skus) {
			String[] record = new String[2];
			record[0] = sku.getVendor();
			record[1] = sku.getProductSku();
			writer.writeNext(record);
			response.getOutputStream().print(record[0]+","+record[1].replace(",", " | ")+"\n");
		}
		writer.close();
		response.getOutputStream().flush();
	}

	@RequestMapping(value = "/api/create", method = RequestMethod.POST)
	@ResponseBody
	public String saveSku(@RequestParam("productSku") String productSku,
			@RequestParam("startingId") String startingId,
			@RequestParam("vendor") String vendor,
			@RequestParam(value = "productIds[]") String[] productIds) {

//		System.out.println(vendor);
//		System.out.println(productSku.replace("-", ""));
//		System.out.println(startingId);
//
//		for (String str : productIds)
//			System.out.println(str);

		Sku sku = new Sku();
		sku.setVendor(vendor);
		sku.setProductSku(productSku.replace("-", ""));
		skuRepository.save(sku);

		return "true";
	}

	@PostMapping("/api/upload")
	public ResponseEntity<?> uploadSku(
			@RequestParam("productSku") String productSku,
			@RequestParam("startingId") String startingId,
			@RequestParam("vendor") String vendor,
			@RequestParam(value = "productIds[]") String[] productIds,
			@RequestParam("files") MultipartFile[] uploadfiles) {
		
//		System.out.println(vendor);
//		System.out.println(productSku);
//		System.out.println(startingId);
//
//		for (String str : productIds)
//			System.out.println(str);

		String uploadedFileName = Arrays.stream(uploadfiles)
				.map(x -> x.getOriginalFilename())
				.filter(x -> !StringUtils.isEmpty(x))
				.collect(Collectors.joining(" , "));

		if (StringUtils.isEmpty(uploadedFileName)) {
			return new ResponseEntity(HttpStatus.OK);
		}

		try {
			saveUploadedFiles(Arrays.asList(uploadfiles));
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		String csvFile = UPLOADED_FOLDER + uploadedFileName;
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(csvFile));
			String[] line;
			String skus = null;
			while ((line = reader.readNext()) != null) {
				skus = line[0];
			}
			String[] sku = skus.split(",");
			
//			for (String str : sku)
//				System.out.println(str.replace("-", ""));

			Sku mySku = new Sku();
			mySku.setVendor(vendor);
			mySku.setProductSku(skus.replace("-", ""));
			skuRepository.save(mySku);

		} catch (IOException e) {
			e.printStackTrace();
		}

		File file = new File(csvFile);
		file.delete();

		return new ResponseEntity(HttpStatus.OK);

	}

	// save file
	private void saveUploadedFiles(List<MultipartFile> files)
			throws IOException {
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue;
			}
			byte[] bytes = file.getBytes();
			Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
			Files.write(path, bytes);
		}
	}

}