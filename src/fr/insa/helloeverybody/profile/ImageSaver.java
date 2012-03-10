package fr.insa.helloeverybody.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

public class ImageSaver {
	public static final String AVATAR_FILE_NAME = "Hello Everybody avatar.jpg";
	
	// Sauvegarde l'avatar
	public static void saveAvatar(Bitmap avatar) {
		File storageDirectory = Environment.getExternalStorageDirectory();
		File avatarFile = new File(storageDirectory, AVATAR_FILE_NAME);
	
		try {
		     FileOutputStream out = new FileOutputStream(avatarFile);
		     avatar.compress(Bitmap.CompressFormat.PNG, 80, out);
		     out.flush();
		     out.close();
		} catch (Exception e) {
		     e.printStackTrace();
		}
	}
	
	// Charge l'avatar
	public static Bitmap getAvatar() {
		File storageDirectory = Environment.getExternalStorageDirectory();
		File avatarFile = new File(storageDirectory, AVATAR_FILE_NAME);
		
		if (avatarFile.exists()) {
			return BitmapFactory.decodeFile(avatarFile.toString());
		}
		
		return null;
	}
}
