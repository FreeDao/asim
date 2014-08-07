package com.view.asim.manager;


import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.view.asim.comm.Constant;



import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;


/**
 * 
 * 安司盾管理工具类.
 * 
 * @author  xuweinan
 */
public class AUKeyManager {
	private final static String TAG = "AUKeyManager";

	public final static String ATTACHED = "attached";
	public final static String DETACHED = "detached";
	public final static String UNKNOWN = "unknown";
	
	public final static String CIP_ALGO = "RSA";
	public final static String PASS_ALGO = "AES";

	private static AUKeyManager aUkeyManager;
	private String mStatus;
	
	private PrivateKey mMyPrivKey = null;
	private PublicKey mMyPubKey = null;
		
	private AUKeyManager() {
		mStatus = DETACHED;
	}

	public static AUKeyManager getInstance() {
		if (aUkeyManager == null) {
			aUkeyManager = new AUKeyManager();
		}
		return aUkeyManager;
	}

	public void destroy() {
		aUkeyManager = null;
	}

	public void setAUKeyStatus(Context cntx, String status) {

		if(!this.mStatus.equals(status)) {
			Log.d(TAG, "AUKey status " + this.mStatus + " changed to " + status);
			this.mStatus = status;

			Intent intent = new Intent(Constant.AUKEY_STATUS_UPDATE);
			intent.putExtra(Constant.AUKEY_STATUS_KEY, status);
			cntx.sendBroadcast(intent);
		}
	}
	
	public String getAUKeyStatus() {
		return mStatus;
	}
	
	/**
	 * 获取本用户的公钥
	 * @return
	 */
	public PublicKey getPublicKey() {
		return mMyPubKey;
	}
	
	/**
	 * 获取本用户的私钥
	 * @return
	 */
	public PrivateKey getPrivateKey() {
		return mMyPrivKey;
	}
	
	public void setPublicKey(PublicKey pub) {
		this.mMyPubKey = pub;
	}
	
	public void setPrivate(PrivateKey priv) {
		this.mMyPrivKey = priv;
	}
	
	/**
	 * 使用口令加密私钥（Base64编码）
	 * @return
	 */
	public String encryptPrivateKey(String passphrase) {
		byte[] seeds = null;
		byte[] raw = mMyPrivKey.getEncoded();
		byte[] encrypted = null;
		
        Cipher cipher;
		try {
			seeds = getRawKey(passphrase.getBytes());
			SecretKeySpec skeySpec = new SecretKeySpec(seeds, PASS_ALGO);      
			cipher = Cipher.getInstance(PASS_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			encrypted = cipher.doFinal(raw);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}      
        
		String result = Base64.encodeToString(encrypted, Base64.DEFAULT);
		return result;
	}
	
	private byte[] getRawKey(byte[] seed) throws NoSuchAlgorithmException{      
        KeyGenerator kgen = KeyGenerator.getInstance(PASS_ALGO);      
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);      
        kgen.init(128, sr);     
        SecretKey skey = kgen.generateKey();      
        byte[] raw = skey.getEncoded();      
        return raw;      
    }   
	
	/**
	 * 使用口令解密还原私钥
	 * @return
	 */
	public PrivateKey decryptPrivateKey(String passphrase, String privKeyEncryped) {
		byte[] seeds = null;
		byte[] raw = Base64.decode(privKeyEncryped, Base64.DEFAULT);
		byte[] decrypted = null;
		
		try {
			seeds = getRawKey(passphrase.getBytes());
			
			SecretKeySpec skeySpec = new SecretKeySpec(seeds, PASS_ALGO);      
	        Cipher cipher = Cipher.getInstance(PASS_ALGO);      
	        cipher.init(Cipher.DECRYPT_MODE, skeySpec);      
	        decrypted = cipher.doFinal(raw);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} 
		
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decrypted);
        KeyFactory keyFactory = null;
        PrivateKey privateKey = null;

		try {
			keyFactory = KeyFactory.getInstance(CIP_ALGO);
			privateKey = keyFactory.generatePrivate(keySpec);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
        return privateKey;
	}
	
	/**
	 * 将公钥编码
	 * @param pubKey
	 * @return
	 */
	public String encodePublicKey(PublicKey pubKey) {
		byte[] keyBytes = pubKey.getEncoded();
		return Base64.encodeToString(keyBytes, Base64.DEFAULT);
	}
	
	/**
	 * 将本用户的公钥编码
	 * @return
	 */
	public String encodePublicKey() {
		byte[] keyBytes = mMyPubKey.getEncoded();
		return Base64.encodeToString(keyBytes, Base64.DEFAULT);
	}
	
	/**
	 * 将公钥解码
	 * @param privKeyEncryped
	 * @return
	 */
	public PublicKey decodePublicKey(String encodedKey) {
		byte[] keyBytes = Base64.decode(encodedKey, Base64.DEFAULT);
		
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = null;
        PublicKey publicKey = null;

		try {
			keyFactory = KeyFactory.getInstance(CIP_ALGO);
			publicKey = keyFactory.generatePublic(keySpec);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
        return publicKey;	
    }
	
	/**
	 * 初始化本用户的密钥
	 * @param passphrase
	 */
	public void initKey() { 
		Cipher cipher = null;
	    KeyPairGenerator keyPairGenerator = null;
		
	     try {
			cipher = Cipher.getInstance(CIP_ALGO);
			keyPairGenerator = KeyPairGenerator.getInstance(CIP_ALGO);

		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (NoSuchPaddingException e1) {
			e1.printStackTrace();
		} 
		
	     
	    KeyPair keyPair = keyPairGenerator.generateKeyPair(); 
	    //获得公钥 
	    mMyPubKey = (PublicKey) keyPair.getPublic(); 
	    //获得私钥  
	    mMyPrivKey = (PrivateKey) keyPair.getPrivate(); 
	}
	
	/**
	 * 用好友的公钥加密数据
	 * @param publicKey base64编码的公钥
	 * @param data 欲加密的明文
	 * @return base64编码的密文
	 */
	public String encryptData(String publicKey, String data) {
		return data;
		/*
		PublicKey pubKey = decodePublicKey(publicKey);
        byte[] plainText = data.getBytes();
        byte[] enBytes = null;

		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(CIP_ALGO);
	        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
	        enBytes = cipher.doFinal(plainText);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		}

        return Base64.encodeToString(enBytes, Base64.DEFAULT);
        */
	}
	
	/**
	 * 用自己的私钥解密数据
	 * @param data base64编码的密文
	 * @return 解密后的明文
	 */
	public String decryptData(String data) {
		return data;
		/*
		Cipher cipher = null;
		byte[] enBytes = null;
		byte[] deBytes = null;
		try {
	
			cipher = Cipher.getInstance(CIP_ALGO);
			cipher.init(Cipher.DECRYPT_MODE, mMyPrivKey);
			//先将转为base64编码的加密后的数据转化为byte数组
			enBytes = Base64.decode(data, Base64.DEFAULT);
			//解密称为byte数组，应该为字符串数组最后转化为字符串
			deBytes = cipher.doFinal(enBytes);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		}
		
		return new String(deBytes);
		*/
	}
}
