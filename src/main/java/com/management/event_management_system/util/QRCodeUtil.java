package com.management.event_management_system.util;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

@Component
public class QRCodeUtil {

	public byte[] generateQRCodeImage(String text, int width, int height) throws Exception {
		BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

		return outputStream.toByteArray();
	}
}
