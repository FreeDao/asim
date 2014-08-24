package com.view.asim.manager;


import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.packet.VCard;

import com.view.asim.comm.ApplicationContext;
import com.view.asim.comm.Constant;
import com.view.asim.model.GroupUser;
import com.view.asim.model.Server;
import com.view.asim.model.User;
import com.view.asim.utils.CharacterParser;
import com.view.asim.utils.CryptoUtil;
import com.view.asim.utils.DateUtil;
import com.view.asim.utils.ImageUtil;
import com.view.asim.utils.StringUtil;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.util.Log;


public class ContacterManager {

	private static final String TAG = "ContacterManager";
	
	/**��ȡ��Phone���ֶ�**/  
    private static final String[] PHONES_PROJECTION = new String[] {  
        Phone.DISPLAY_NAME, Phone.NUMBER };  
     
    /**��ϵ����ʾ����**/  
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;  
      
    /**�绰����**/  
    private static final int PHONES_NUMBER_INDEX = 1;  
    
    // ��¼�û�����
    public static User userMe = null;
    
	// ������ϵ��
	public static Map<String, User> contacters = null;
	
	// Ⱥ����
	public static Map<String, GroupUser> groupUsers = null;
	
	// �ֻ�ͨѶ¼��ϵ��
	public static Map<String, String> phoneContacters = null;

	/**
	 * ��ʼ����ϵ���б�
	 */
	public static void init(XmppConnectionManager man) {
		contacters = new HashMap<String, User>();
		groupUsers = new HashMap<String, GroupUser>();
		phoneContacters = new HashMap<String, String>();
		initPhoneContacts();
		man.registerConnectionChangeListener(TAG + ":init roster task", new XmppConnectionChangeListener() {

			@Override
			public void newConnection(XMPPConnection connection) {
				Log.d(TAG, "new conn, init roster contacters");

				Roster roster = connection.getRoster();
				for (RosterEntry entry : roster.getEntries()) {
					Log.d(TAG, "init roster entry:" + entry.getUser() + ", item status is " + entry.getStatus() +
							", item type is " + entry.getType());
					
					// �����δ��ӳɹ��ĺ��ѣ�����ʾ
					if (entry.getType().equals(ItemType.none)) {
						continue;
					}
					contacters.put(entry.getUser(), getUserByRosterEntry(connection, entry, roster));
				}
							
			}
			
		});
	}
	
	public static void setUserMe(User me) {
		userMe = me;
		for (GroupUser grp: userMe.getGroupList()) {
			groupUsers.put(grp.getName(), grp);
		}		
	}

	public static void initPhoneContacts() {
		Log.d(TAG, "init phone contacters start on " + DateUtil.getCurDateStr());

		phoneContacters.clear();
		ContentResolver resolver = ApplicationContext.get().getContentResolver();  
		  
	    // ��ȡ�ֻ���ϵ��  
	    Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);  
	  
	    if (phoneCursor != null) {  
	        while (phoneCursor.moveToNext()) {  
		        //�õ��ֻ�����  
		        String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  
		        //���ֻ�����Ϊ�յĻ���Ϊ���ֶ� ������ǰѭ��  
		        if (TextUtils.isEmpty(phoneNumber.trim()))  
		            continue;  
		          
		        //�õ���ϵ������  
		        String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);  
		        		        
		        if(phoneNumber.contains("-")) {
		        	phoneNumber = phoneNumber.replace("-", "");
		        }
		        
		        if(phoneNumber.contains(" ")) {
		        	phoneNumber = phoneNumber.replace(" ", "");
		        }
		        
		        if(phoneNumber.contains("+86")) {
		        	phoneNumber = phoneNumber.replace("+86", "");
		        }
		        
		        phoneContacters.put(phoneNumber, contactName);
				//Log.d(TAG, "init phone contacter: " + phoneNumber + ", " + contactName);

	        }  
	  
	        phoneCursor.close();
	    }
		Log.d(TAG, "init phone contacters end on " + DateUtil.getCurDateStr());

	}
	
	/**
	 * ������е���ϵ���б�
	 * 
	 * @return
	 */
	public static List<User> getContacterList() {
		if (contacters == null)
			throw new RuntimeException("contacters is null");

		List<User> userList = new ArrayList<User>();

		for (String key : contacters.keySet())
			userList.add(contacters.get(key));

		return userList;
	}

	/**
	 * ������е�Ⱥ�����б�
	 * 
	 * @return
	 */
	public static List<GroupUser> getGroupsList() {
		if (groupUsers == null)
			throw new RuntimeException("groupUsers is null");

		List<GroupUser> groupList = new ArrayList<GroupUser>();

		for (String key : groupUsers.keySet())
			groupList.add(groupUsers.get(key));

		return groupList;
	}
	
	/**
	 * ���ݱ��˺ͳ�Ա�б�����Ⱥ�����û�
	 * @param members
	 * @return
	 */
	public static GroupUser createGroupUserByMember(List<User> members) {
		GroupUser grpUser = new GroupUser();
		String grpName = userMe.getName();
		String grpNick = userMe.getNickName();
		for(User u: members) {
			grpName += ":" + u.getName();
			grpNick += "��" + u.getNickName();
			grpUser.addMember(u);
		}
 
		try {
			grpName = CryptoUtil.md5(grpName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		grpUser.setName(grpName);
		grpUser.setNickName(grpNick + "��Ⱥ��");
		grpUser.setOwnerName(userMe.getName());
		return grpUser;
	}
	
    /** 
     * ���汾�û��� VCard ��Ϣ
     *  
     * @param conn 
     * @param user 
     * @return 
     * @throws XMPPException 
     */ 
	public static void saveUserVCard(XMPPConnection conn, User user, VCard card) {
		if (card == null || user == null || conn == null) {
			return;
		}
		
		try {
			// �ǳƲ�����Ϊ��
			card.setNickName(user.getNickName());
			
			if (card.getJabberId() != null && !card.getJabberId().equals(user.getJID())) {
				Log.e(TAG, "User(" + user.getJID() + ") does not match VCard(" + card.getJabberId() + ")!");
				return;
			}

			if (card.getJabberId() == null) {
				card.setJabberId(user.getJID());
			}
			
			if (user.getGender() != null) {
				card.setField(User.VCARD_FIELD_GENDER, user.getGender());
			}
			
			if (user.getGlobalID() != null) {
				card.setField(User.VCARD_FIELD_GLOBALID, user.getGlobalID());
			}
			
			if (user.getLocation() != null) {
				card.setAddressFieldHome(User.VCARD_FIELD_LOCATION, user.getLocation());
			}
			
			if (user.getHeadImg() != null) {
				card.setAvatar(ImageUtil.bitmapToByte(user.getHeadImg()));
			}
			
			if (user.getRemark() != null) {
				card.setField(User.VCARD_FIELD_REMARK, user.getRemark());
			}
			
			if (user.getSecurity() != null) {
				card.setField(User.VCARD_FIELD_SECURITY, user.getSecurity());
			}
			
			if (user.getPublicKey() != null) {
				card.setField(User.VCARD_FIELD_PUBLICKEY, user.getPublicKey());
			}
			
			if (user.getPrivateKey() != null) {
				card.setField(User.VCARD_FIELD_PRIVATEKEY, user.getPrivateKey());
			}
			
			if (user.getGroupList().size() > 0) {
				card.setField(User.VCARD_FIELD_GROUPINFO, user.dumpGroupList());
			}
			
			card.save(conn);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return;
	}

	public static void saveUserVCard(XMPPConnection conn, User user) {
		VCard card = new VCard();
		try {
			card.load(conn);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
			
		saveUserVCard(conn, user, card);
	}
	
	/**
	 * �ӷ��������º�����Ϣ��ӵ����ػ���
	 * @param jid
	 */
	public static void loadAndUpdateContacter(String jid) {
		XMPPConnection conn = XmppConnectionManager.getInstance().getConnection();
		User user = getUserByName(conn, StringUtil.getUserNameByJid(jid));

		contacters.put(jid, user);
	}
	/**
	 * 根据jid获取User信息
	 * @param jid
	 */
	public static User getUserInfoByJid(String jid){
		if(contacters.containsKey(jid)){
			return contacters.get(jid);
		}else{
			loadAndUpdateContacter(jid);
			return contacters.get(jid);
		}
	}
    /** 
     * ��ȡ�û� VCard ��Ϣ 
     *  
     * @param conn 
     * @param name 
     * @return 
     * @throws XMPPException 
     */  
    public static VCard getUserVCard(XMPPConnection conn, String name) {  
        if (conn == null || name == null || name.trim().length() <= 0)  
            return null;
        
        VCard vcard = new VCard();  
        try {  
			vcard.load(conn, StringUtil.getJidByName(name, conn.getServiceName()));
		} catch (XMPPException e) {  
            e.printStackTrace();  
        }  
        return vcard;  
    }
    
    /** 
     * ��ȡ���� VCard ��Ϣ 
     *  
     * @param conn 
     * @param name 
     * @return 
     * @throws XMPPException 
     */  
    public static VCard getMyVCard(XMPPConnection conn) {  
        if (conn == null)  
            return null;
        
        VCard vcard = new VCard();  
        try {  
            vcard.load(conn);  
		} catch (XMPPException e) {  
            e.printStackTrace();  
        }    
        return vcard;  
    }

    /** 
     * ��ȡ�û�ͷ����Ϣ 
     *  
     * @param conn 
     * @param name 
     * @return 
     */  
    public static Bitmap getUserImage(XMPPConnection conn, String name) {  
        if (conn == null)  
            return null;  
        
        byte[] bais = null;  
        try {  
            VCard vcard = new VCard();  
            if (name == "" || name == null || name.trim().length() <= 0) {  
                return null;  
            }  
            vcard.load(conn, StringUtil.getJidByName(name, conn.getServiceName()));  
  
            if (vcard == null || vcard.getAvatar() == null)  
                return null;  
            bais = vcard.getAvatar();  
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;  
        }  
        return ImageUtil.byteToBitmap(bais);
    } 
    

    /** 
     * ��ȡ�û�ͷ����Ϣ 
     *  
     * @param conn 
     * @param card 
     * @return 
     */  
    public static Bitmap getUserImage(XMPPConnection conn, VCard card) {  
        if (conn == null)  
            return null;  
        
        byte[] bais = null;  
        try {  
            if (card == null || card.getAvatar() == null)  
                return null;  
            bais = card.getAvatar();  
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;  
        }  
        return ImageUtil.byteToBitmap(bais);
    } 
    
    public static User getUserByNameAndVCard(String name, VCard vcard) {
		User user = new User();
		Server srv = AppConfigManager.getInstance().getServer();
		
		user.setName(name);
		user.setJID(StringUtil.getJidByName(name, srv.getXmppServiceName()));
		user.setNickName(vcard.getNickName());
		user.setGender(vcard.getField(User.VCARD_FIELD_GENDER));
		user.setGlobalID(vcard.getField(User.VCARD_FIELD_GLOBALID));
		user.setLocation(vcard.getAddressFieldHome(User.VCARD_FIELD_LOCATION));
		user.setRemark(vcard.getField(User.VCARD_FIELD_REMARK));
		user.setSecurity(vcard.getField(User.VCARD_FIELD_SECURITY));
		
		// FIXME:
		//user.loadGroupList(vcard.getField(User.VCARD_FIELD_GROUPINFO));
		
		String cellphone = StringUtil.getCellphoneByName(user.getName());
		if (phoneContacters != null && phoneContacters.containsKey(cellphone)) {
			user.setContactName(phoneContacters.get(cellphone));
		}

		XMPPConnection conn = XmppConnectionManager.getInstance().getConnection();
		user.setHeadImg(getUserImage(conn, vcard));

		if (user.getNickName() != null) {
			user.setNamepy(CharacterParser.getPingYin(user.getNickName()));
			user.setNameFirstChar(CharacterParser.getPinYinHeadChar(user.getNickName()));
			user.setNamecode(CharacterParser.getnamecode(user.getNameFirstChar()));
		}
		
		String pinyin = user.getNamepy();
		user.setSortLetters("#");

		if(pinyin != null) {
			String sortString = pinyin.substring(0, 1).toUpperCase();
			if (sortString.matches("[A-Z]")) {
				user.setSortLetters(sortString.toUpperCase());
			}
		}
		
		Log.d(TAG, "getUserByNameAndVCard: " + user);
		return user;
	}
    
    
	/**
	 * ����RosterEntry����һ������User
	 * 
	 * @param entry
	 * @return
	 */
	public static User getUserByRosterEntry(XMPPConnection conn, RosterEntry entry, Roster roster) {
		//User user = new User();
		String name = StringUtil.getUserNameByJid(entry.getUser());
		VCard vcard = getUserVCard(conn, name);
		
		return getUserByNameAndVCard(name, vcard);

		/*
		user.setName(StringUtil.getUserNameByJid(entry.getUser()));
		user.setJID(entry.getUser());
		user.setNickName(vcard.getNickName());
		user.setGender(vcard.getField(User.VCARD_FIELD_GENDER));
		user.setGlobalID(vcard.getField(User.VCARD_FIELD_GLOBALID));
		user.setLocation(vcard.getAddressFieldHome(User.VCARD_FIELD_LOCATION));
		user.setRemark(vcard.getField(User.VCARD_FIELD_REMARK));
		user.setSecurity(vcard.getField(User.VCARD_FIELD_SECURITY));
		user.loadGroupList(vcard.getField(User.VCARD_FIELD_GROUPINFO));
		
		String cellphone = StringUtil.getCellphoneByName(user.getName());
		if (phoneContacters != null && phoneContacters.containsKey(cellphone)) {
			user.setContactName(phoneContacters.get(cellphone));
		}

		user.setHeadImg(getUserImage(conn, vcard));
		
		if (user.getNickName() != null) {
			user.setNamepy(CharacterParser.getPingYin(user.getNickName()));
			user.setNameFirstChar(CharacterParser.getPinYinHeadChar(user.getNickName()));
			user.setNamecode(CharacterParser.getnamecode(user.getNameFirstChar()));
		}
		
		String pinyin = user.getNamepy();
		String sortString = pinyin.substring(0, 1).toUpperCase();
		if (sortString.matches("[A-Z]")) {
			user.setSortLetters(sortString.toUpperCase());
		} else {
			user.setSortLetters("#");
		}
		
		Log.d(TAG, "getUserByRosterEntry: " + user);

		return user;
		*/
	}

	/**
	 * ����name����һ��User
	 * 
	 * @param name
	 * @return
	 */
	public static User getUserByName(XMPPConnection conn, String name) {
		//User user = new User();
		VCard vcard = getUserVCard(conn, name);

		return getUserByNameAndVCard(name, vcard);
		/*
		user.setName(name);
		user.setJID(StringUtil.getJidByName(name, conn.getServiceName()));
		user.setNickName(vcard.getNickName());
		user.setGender(vcard.getField(User.VCARD_FIELD_GENDER));
		user.setGlobalID(vcard.getField(User.VCARD_FIELD_GLOBALID));
		user.setLocation(vcard.getAddressFieldHome(User.VCARD_FIELD_LOCATION));
		user.setRemark(vcard.getField(User.VCARD_FIELD_REMARK));
		user.setSecurity(vcard.getField(User.VCARD_FIELD_SECURITY));
		user.loadGroupList(vcard.getField(User.VCARD_FIELD_GROUPINFO));
		
		String cellphone = StringUtil.getCellphoneByName(user.getName());
		if (phoneContacters != null && phoneContacters.containsKey(cellphone)) {
			user.setContactName(phoneContacters.get(cellphone));
		}

		user.setHeadImg(getUserImage(conn, vcard));

		if (user.getNickName() != null) {
			user.setNamepy(CharacterParser.getPingYin(user.getNickName()));
			user.setNameFirstChar(CharacterParser.getPinYinHeadChar(user.getNickName()));
			user.setNamecode(CharacterParser.getnamecode(user.getNameFirstChar()));
		}
		
		String pinyin = user.getNamepy();
		user.setSortLetters("#");

		if(pinyin != null) {
			String sortString = pinyin.substring(0, 1).toUpperCase();
			if (sortString.matches("[A-Z]")) {
				user.setSortLetters(sortString.toUpperCase());
			}
		}
		
		Log.d(TAG, "getUserByName: " + user);

		return user;
		*/
	}
	/**
	 * �޸�������ѵ��ǳ�
	 * 
	 * @param user
	 * @param nickname
	public static void setNickname(User user, String nickname,
			XMPPConnection connection) {
		RosterEntry entry = connection.getRoster().getEntry(user.getJID());

		entry.setName(nickname);
	}
	 */

	/**
	 * ��һ��������ӵ�һ������
	 * 
	 * @param user
	 * @param groupName
	public static void addUserToGroup(final User user, final String groupName,
			final XMPPConnection connection) {
		if (groupName == null || user == null)
			return;
		// ��һ��rosterEntry��ӵ�group����PacketCollector���������߳�
		new Thread() {
			@Override
			public void run() {
				RosterGroup group = connection.getRoster().getGroup(groupName);
				// ������Ѿ����ھ���ӵ�����飬�����ڴ���һ����
				RosterEntry entry = connection.getRoster().getEntry(
						user.getJID());
				try {
					if (group != null) {
						if (entry != null)
							group.addEntry(entry);
					} else {
						RosterGroup newGroup = connection.getRoster()
								.createGroup(groupName);
						if (entry != null)
							newGroup.addEntry(entry);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	 */

	/**
	 * ��һ�����Ѵ�����ɾ��
	 * 
	 * @param user
	 * @param groupName
	public static void removeUserFromGroup(final User user,
			final String groupName, final XMPPConnection connection) {
		if (groupName == null || user == null)
			return;
		new Thread() {
			@Override
			public void run() {
				RosterGroup group = connection.getRoster().getGroup(groupName);
				if (group != null) {
					try {
						System.out.println(user.getJID() + "----------------");
						RosterEntry entry = connection.getRoster().getEntry(
								user.getJID());
						if (entry != null)
							group.removeEntry(entry);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	 */

	/*
	 * 
	public static class MRosterGroup {
		private String name;
		private List<User> users;

		public MRosterGroup(String name, List<User> users) {
			this.name = name;
			this.users = users;
		}

		public int getCount() {
			if (users != null)
				return users.size();
			return 0;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<User> getUsers() {
			return users;
		}

		public void setUsers(List<User> users) {
			this.users = users;
		}

	}
	*/
	
	/**
	 * 
	 * ����jid����û��ǳ�
	 * 
	 * @param Jid
	 * @param connection
	 * @return
	 * @author shimiso
	 * @update 2012-6-28 ����10:49:14
	public static User getNickname(String Jid, XMPPConnection connection) {
		Roster roster = connection.getRoster();
		for (RosterEntry entry : roster.getEntries()) {
			String params = entry.getUser();
			if (params.split("/")[0].equals(Jid)) {
				return transEntryToUser(entry, roster);
			}
		}
		return null;

	}
	 */

	/**
	 * ��ӷ��� .
	 * 
	 * @param user
	 * @param groupName
	 * @param connection
	 * @author shimiso
	 * @update 2012-6-28 ����3:30:32
	public static void addGroup(final String groupName,
			final XMPPConnection connection) {
		if (StringUtil.empty(groupName)) {
			return;
		}

		// ��һ��rosterEntry��ӵ�group����PacketCollector���������߳�
		new Thread() {
			@Override
			public void run() {

				try {
					RosterGroup g = connection.getRoster().getGroup(groupName);
					if (g != null) {
						return;
					}
					connection.getRoster().createGroup(groupName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	 */

	/**
	 * �����������
	 * 
	 * @return
	public static List<String> getGroupNames(Roster roster) {

		List<String> groupNames = new ArrayList<String>();
		for (RosterGroup group : roster.getGroups()) {
			Log.d(TAG, group.getName());
			groupNames.add(group.getName());
		}
		return groupNames;
	}
	 */

	
	/**
	 * ���һ����ϵ��
	 * 
	 * @param userJid
	 *            ��ϵ��JID
	 * @param nickname
	 *            ��ϵ���ǳ�
	 * @param groups
	 *            ��ϵ����ӵ���Щ��
	 * @throws XMPPException
	 */
	public static void createSubscriber(String userJid, String nickname,
			String[] groups) throws XMPPException {
		XmppConnectionManager.getInstance().getConnection().getRoster()
				.createEntry(userJid, nickname, groups);
		
	}

	/**
	 * ɾ��һ����ϵ��
	 * 
	 * @param userJid
	 *            ��ϵ�˵�JID
	 * @throws XMPPException
	 */
	public static void removeSubscriber(String userJid) throws XMPPException {
		contacters.remove(userJid);
		RosterEntry entry = XmppConnectionManager.getInstance().getConnection().getRoster().getEntry(userJid);
		if (entry != null)
			XmppConnectionManager.getInstance().getConnection().getRoster().removeEntry(entry);
		sendSubscribe(Presence.Type.unsubscribe, userJid);
	}
	
	/**
	 * �ظ�һ��presence��Ϣ���û�
	 * 
	 * @param type
	 * @param to
	 */
	public static void sendSubscribe(Presence.Type type, String to) {
		Presence presence = new Presence(type);
		presence.setTo(to);
		XmppConnectionManager.getInstance().getConnection()
				.sendPacket(presence);
	}
	/**
	 * 
	 * �����ǳ������û�
	 * 
	 * @param  searchUserByName
	 * @author xuweinan
	 */
	public static List<User> searchUserByName(String name) throws XMPPException {

		List<User> results = new ArrayList<User>();
		
		XMPPConnection conn = XmppConnectionManager.getInstance().getConnection();
		UserSearchManager search = new UserSearchManager(conn);
		Form searchForm = search.getSearchForm("search." + conn.getServiceName());
		Form answerForm = searchForm.createAnswerForm();
		answerForm.setAnswer("Name", true);
		answerForm.setAnswer("search", name);
		
		ReportedData data = search.getSearchResults(answerForm, "search." + conn.getServiceName());
		Iterator<Row> it = data.getRows();
		Row row = null;
		User user = null;
		while (it.hasNext()) {
			row = it.next();
			user = getUserByName(conn, row.getValues("Username").next().toString());
			
			Log.d(TAG, "search succ: " + user);
			results.add(user);
		}
		
		if (results.size() == 0) 
			Log.d(TAG, "search failed by name " + name);

		return results;
	}
	
	/**
	 * 
	 * �����ֻ����������û�
	 * 
	 * @param  searchUserByName
	 * @author xuweinan
	 */
	public static List<User> searchUserByCellphone(String cellphone) throws XMPPException {

		List<User> results = new ArrayList<User>();
		
		XMPPConnection conn = XmppConnectionManager.getInstance().getConnection();
		UserSearchManager search = new UserSearchManager(conn);
		Form searchForm = search.getSearchForm("search." + conn.getServiceName());
		Form answerForm = searchForm.createAnswerForm();
		answerForm.setAnswer("Username", true);
		answerForm.setAnswer("search", StringUtil.getNameByCellphone(cellphone));
		
		ReportedData data = search.getSearchResults(answerForm, "search." + conn.getServiceName());
		Iterator<Row> it = data.getRows();
		Row row = null;
		User user = null;
		String name = null;
		String nick = null;
		while (it.hasNext()) {
			row = it.next();
			//user = getUserByName(conn, row.getValues("Username").next().toString());
			name = row.getValues("Username").next().toString();
			nick = row.getValues("Name").next().toString();
			user = new User();
			user.setName(name);
			user.setJID(StringUtil.getJidByName(name));
			user.setNickName(nick);
			Log.d(TAG, "search succ: " + name  + ", " + nick);
			results.add(user);
		}
		if (results.size() == 0) 
			Log.d(TAG, "search failed by cellphone " + cellphone);

		return results;
	}

}
