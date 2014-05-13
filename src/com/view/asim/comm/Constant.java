package com.view.asim.comm;

import android.os.Environment;

public class Constant {
	/**
	 * ���е�action�ļ����ı���Ҫ��"ACTION_"��ͷ
	 * 
	 */

	/**
	 * ��������ɾ����ACTION��KEY
	 */
	public static final String ROSTER_DELETED = "roster.deleted";
	public static final String ROSTER_DELETED_KEY = "roster.deleted.key";

	/**
	 * �������и��µ�ACTION��KEY
	 */
	public static final String ROSTER_UPDATED = "roster.updated";
	public static final String ROSTER_UPDATED_KEY = "roster.updated.key";

	/**
	 * �����������ӵ�ACTION��KEY
	 */
	public static final String ROSTER_ADDED = "roster.added";
	public static final String ROSTER_ADDED_KEY = "roster.added.key";

	/**
	 * �������г�Ա״̬�иı��ACTION��KEY
	 */
	public static final String ROSTER_PRESENCE_CHANGED = "roster.presence.changed";
	public static final String ROSTER_PRESENCE_CHANGED_KEY = "roster.presence.changed.key";

	/**
	 * �յ�������������
	 */
	public static final String ROSTER_SUBSCRIPTION = "roster.subscribe";
	public static final String ROSTER_SUB_FROM = "roster.subscribe.from";
	public static final String NOTICE_ID = "notice.id";

	/**
	 * ����Ϣ
	 */
	public static final String NEW_MESSAGE_ACTION = "roster.newmessage";
	public static final String NEW_CTRL_MESSAGE_ACTION = "roster.newctrlmessage";
	
	/**
	 * ����������Ϣ֪ͨ
	 */
	public static final String RECV_OFFLINE_MSG_ACTION = "roster.offlinemessage";
	
	/**
	 * �����ļ�
	 */
	public static final String SEND_FILE_ACTION = "immessage.sendfile";
	public static final String SEND_FILE_RESULT_ACTION = "immessage.sendfile.result";
	public static final String FILE_PROGRESS_ACTION = "immessage.sendfile.progress";
	public static final String SEND_FILE_KEY_MESSAGE = "immessage.sendfile.key.message";
	public static final String SEND_FILE_KEY_PROGRESS = "immessage.sendfile.key.progress";

	
	/**
	 * ������Ϣ
	 */
	public static final String SEND_MESSAGE_ACTION = "immessage.sendmessage";
	public static final String SEND_MESSAGE_RESULT_ACTION = "immessage.sendmessage.result";
	public static final String SEND_MESSAGE_KEY_MESSAGE = "immessage.sendmessage.key.message";
	
	/**
	 * Ⱥ������/�˳�/����
	 */
	public static final String GROUP_INVITE_ACTION = "immessage.group.invite";
	public static final String GROUP_QUIT_ACTION = "immessage.group.quit";
	public static final String GROUP_UPDATE_ACTION = "immessage.group.update";
	public static final String GROUP_ACTION_KEY_INFO = "immessage.group.key.info";
	
	/**
	 * ����Ƶ���ļ�Ԥ��
	 */
	public static final String PREVIEW_AUDIO = "preview.audio";
	public static final String PREVIEW_VIDEO = "preview.video";
	public static final String PREVIEW_IMAGE = "preview.image";
	public static final String PREVIEW_FILE  = "preview.file";

	/**
	 * ��˾��״̬����
	 */
	public static final String AUKEY_STATUS_UPDATE = "aukey.status.update";

	/**
	 * �ҵ���Ϣ
	 */
	public static final String MY_NEWS = "my.news";
	public static final String MY_NEWS_DATE = "my.news.date";

	/**
	 * ������������
	 */
	public static final String LOGIN_SET = "eim_login_set";// ��¼����
	public static final String USERNAME = "username";// �˻�
	public static final String PASSWORD = "password";// ����
	public static final String XMPP_HOST = "xmpp_host";// ��ַ
	public static final String XMPP_PORT = "xmpp_port";// �˿�
	public static final String XMPP_SERVICE_NAME = "xmpp_service_name";// ������
	public static final String DATA_ROOT_PATH = "data_root_path"; // ���ݴ��·���ĸ�Ŀ¼

	/**
	 * ���ӷ�����/�˻���¼/ע��/��������ֵ
	 */
	public static final int SERVER_SUCCESS = 0;// �ɹ�
	public static final int HAS_NEW_VERSION = 1;// �����°汾
	public static final int IS_NEW_VERSION = 2;// ��ǰ�汾Ϊ����
	public static final int LOGIN_ERROR_ACCOUNT_PASS = 3;// �˺Ż����������
	public static final int SIGNUP_ERROR_ACCOUNT_PASS = 4;// �˺Ż����������
	public static final int SERVER_UNAVAILABLE = 5;// �޷����ӵ�������
	public static final int UNKNOWN_ERROR = 6;// δ֪����
	public static final int NONE_RESULTS = 7;// ����ʧ�ܴ���

	public static final String XMPP_CONNECTION_CLOSED = "xmpp_connection_closed";// �����ж�

	public static final String LOGIN = "login"; // ��¼
	public static final String RELOGIN = "relogin"; // ���µ�¼

	/**
	 * �����б� ����
	 */
	public static final String ALL_FRIEND = "���к���";// ���к���
	public static final String NO_GROUP_FRIEND = "δ�������";// ���к���
	/**
	 * ϵͳ��Ϣ
	 */
	public static final String ACTION_SYS_MSG = "action_sys_msg";// ��Ϣ���͹ؼ���
	public static final String MSG_TYPE = "broadcast";// ��Ϣ���͹ؼ���
	public static final String SYS_MSG = "sysMsg";// ϵͳ��Ϣ�ؼ���
	public static final String SYS_MSG_DIS = "ϵͳ��Ϣ";// ϵͳ��Ϣ
	public static final String ADD_FRIEND_QEQUEST = "��������";// ϵͳ��Ϣ�ؼ���
	/**
	 * ����ĳ���������ص�״ֵ̬
	 */
	public static final int SUCCESS = 0;// ����
	public static final int FAIL = 1;// ������
	public static final int UNKNOWERROR = 2;// ����Ī���Ĵ���.
	public static final int NETWORKERROR = 3;// �������
	/***
	 * ��ҵͨѶ¼�����û������û���ȥ������Ա�е����������Ƿ��������֯
	 */
	public static final int containsZz = 0;
	/***
	 * �������������ϵ���б�xml��ҳ����
	 */
	public static final String currentpage = "1";// ��ǰ�ڼ�ҳ
	public static final String pagesize = "1000";// ��ǰҳ������

	/***
	 * ��������xml��������
	 */
	public static final String add = "00";// ����
	public static final String rename = "01";// ����
	public static final String remove = "02";// ����

	/**
	 * ������
	 */
	/**
	 * ������״̬acttion
	 * 
	 */
	public static final String ACTION_RECONNECT_STATE = "action_reconnect_state";
	/**
	 * ����������״̬�Ĺػ��ӣ��ķŵ�intent�Ĺؼ���
	 */
	public static final String RECONNECT_STATE = "reconnect_state";
	/**
	 * ���������ӣ�
	 */
	public static final boolean RECONNECT_STATE_SUCCESS = true;
	public static final boolean RECONNECT_STATE_FAIL = false;
	/**
	 * �Ƿ����ߵ�SharedPreferences����
	 */
	public static final String PREFENCE_USER_STATE = "prefence_user_state";
	public static final String IS_ONLINE = "is_online";
	
	/**
	 * ��ҳ��ע��ҳ��֮��Ľ�����
	 */
	public static final int SIGNUP_RESULT = 0;
	
	/**
	 * Activity ֮��� requestCode
	 */
	
	// �������������Ϣ�޸Ľ���
	public final static int REQCODE_MOD_REMARK = 0;
	public final static int REQCODE_MOD_LOCATION = 1;
	public final static int REQCODE_MOD_NICKNAME = 2;
	public final static int REQCODE_MOD_AVATAR_BY_GALLERY = 3;
	public final static int REQCODE_MOD_AVATAR_BY_CAPTURE = 4;
	public final static int REQCODE_MOD_AVATAR_CROP = 5;
	
	// �������� intent action
	public final static int REQCODE_IMAGE_PICK = 10;
	public final static int REQCODE_VIDEO_PICK = 11;
	public final static int REQCODE_TAKE_PICTURE = 12;
	public final static int REQCODE_FILE_PICK = 13;

	// ��������ѡ����ϵ�˽���
	public final static int REQCODE_SELECT_USERS = 20;
	
	// ����������ĺ󼴷ٽ����� action
	public final static int REQCODE_BURN_AFTER_READ = 30;

	// ͼ������
	public final static String FILE_STORAGE_HOST = "com-viewiot-mobile-asim.qiniudn.com";
	public final static String DUMPS_STORAGE_HOST = "com-viewiot-mobile-asim-dumps.qiniudn.com";
	
	// ���ݴ���ڴ洢�������·������
	public static String SDCARD_ROOT_PATH = null;
	public final static String ASIM_ROOT_PATH = "/asim"; 
	public final static String DB_PATH = ASIM_ROOT_PATH + "/database"; 
	public final static String IMAGE_PATH = ASIM_ROOT_PATH + "/image";
	public final static String AUDIO_PATH = ASIM_ROOT_PATH + "/audio";
	public final static String VIDEO_PATH = ASIM_ROOT_PATH + "/video";
	public final static String FILE_PATH = ASIM_ROOT_PATH + "/file";
	public final static String CACHE_PATH = ASIM_ROOT_PATH + "/cache";
	public final static String LOG_PATH = ASIM_ROOT_PATH + "/log";

	public final static String FILE_SUFFIX = ".src";
	public final static String THUMB_SUFFIX = ".thumb";
	public final static String LOG_PREFIX = "normal-";
	public final static String CRASH_PREFIX = "crash-";
	
	// ���س�ʱʱ�䣨10���ӣ�
	public final static int FILE_DOWNLOAD_TIMEOUT = 1000 * 60 * 10;

	
}
