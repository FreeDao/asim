package com.view.asim.comm;

import android.os.Environment;

public class Constant {
	
	/**
	 * ���߼����� ACTION 
	 */
	public static final String OTA_CHECK_ACTION = "ota.checknow";

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
	 * ֪ͨ����֪ͨ
	 */
	public static final String RECONN_ACTION = "xmpp.reconn";
	
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
	 * Զ��������Ϣ
	 */
	public static final String REMOTE_DESTROY_ACTION = "immessage.remotedestroy";
	public static final String REMOTE_DESTROY_KEY_MESSAGE = "immessage.remotedestroy.key.message";

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
	public static final String AUKEY_STATUS_KEY = "aukey.status.key";
	

	/**
	 * �ҵ���Ϣ
	 */
	public static final String MY_NEWS = "my.news";
	public static final String MY_NEWS_DATE = "my.news.date";

	/**
	 * IM ����
	 */
	public static final String IM_SET_PREF = "asim_im_settings_pref";// ��¼����
	public static final String USERNAME = "username";// �˻�
	public static final String PASSWORD = "password";// ����
	public static final String XMPP_HOST = "xmpp_host";// ��ַ
	public static final String XMPP_PORT = "xmpp_port";// �˿�
	public static final String XMPP_SERVICE_NAME = "xmpp_service_name";// ������
	public static final String DATA_ROOT_PATH = "data_root_path"; // ���ݴ��·���ĸ�Ŀ¼
	public static final String XMPP_RESOURCE_NAME = "asim";

	/**
	 * SIP ����
	 */
	public static final String SIP_SET_PREF = "asim_sip_settings_pref";// ��¼����

	/**
	 * ��������ַ
	 */
	public static final String IM_SERVICE_HOST = "112.124.32.193";
	public static final int IM_SERVICE_PORT = 5222;
	public static final String IM_SERVICE_NAME = "112.124.32.193";
	public static final String VOIP_SERVICE_HOST = "121.40.69.120";
	public static final int VOIP_SERVICE_PORT = 6060;
	public static final String VOIP_STUN_SERVER = "121.40.69.120";
	
	
	/**
	 * ���ӷ�����/�˻���¼/ע��/��������ֵ
	 */
	public static final int SERVER_SUCCESS = 0;// �ɹ�
	public static final int HAS_NEW_VERSION = 1;// �����°汾
	public static final int IS_NEW_VERSION = 2;// ��ǰ�汾Ϊ����
	public static final int LOGIN_ERROR_ACCOUNT_PASS = 3;// �˺Ż����������
	public static final int LOGIN_ERROR_DUPLICATED = 4;// �ظ���¼
	public static final int SIGNUP_ERROR_ACCOUNT_PASS = 5;// �˺Ż����������
	public static final int SERVER_UNAVAILABLE = 6;// �޷����ӵ�������
	public static final int UNKNOWN_ERROR = 7;// δ֪����
	public static final int NONE_RESULTS = 8;// ����ʧ�ܴ���
	
	/**
	 * ���ݿ�汾: 
	 * V0.4.28�����ݿ�汾��V1����ʼ�汾��
	 * V0.5.14�����ݿ�汾��V2���޸��� im_notice ��
	 * V0.6.25�����ݿ�汾��V3���޸��� im_notice �� im_msg_his ��
	 * V0.7.15�����ݿ�汾��V4�������� SIP ��ر�
	 */
	public static final int DB_VERSION = 4;

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
	 * ������״̬action
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
	public final static int REQCODE_CAPTURE_VIDEO = 14;

	// ��������ѡ����ϵ�˽���
	public final static int REQCODE_SELECT_USERS = 20;
	
	// ����������ĺ󼴷ٽ����� action
	public final static int REQCODE_BURN_AFTER_READ = 30;

	// IM ��Ϣ��Դ��
	public final static String FILE_STORAGE_HOST = "com-viewiot-mobile-asim.qiniudn.com";
	
	// App ��־��
	public final static String DUMPS_STORAGE_HOST = "com-viewiot-mobile-asim-dumps.qiniudn.com";

	// App OTA��
	public final static String OTA_STORAGE_HOST = "com-viewiot-mobile-asim-ota.qiniudn.com";
	public final static String OTA_CHECK_STATUS_PATH = "/upgrade_rule.json";

	
	/**
	 * ���ݻ���Ŀ¼�ṹ
	 *       asim
	 *         |
	 *         |--- cache (����ƵҲ�ݴ��ڴ�)
	 *         |--- log
	 *         |--- data
	 *                |--- �û� A
	 *                |      |
	 *                |      |--- database
	 *                |      |--- temp
	 *                |      |--- image
	 *                |      |      |
	 *                |      |      |--- ���� A
	 *                |      |      |--- ���� B
	 *                |      |--- audio
	 *                |      |      |
	 *                |      |      |--- ���� A
	 *                |      |      |--- ���� B
	 *                |      |--- video
	 *                |      |      |
	 *                |      |      |--- ���� A
	 *                |      |      |--- ���� B
	 *                |      |--- file
	 *                |             |
	 *                |             |--- ���� A
	 *                |             |--- ���� B
	 *                |--- �û� B
	 */
	
	// ���ݴ���ڴ洢�������·������
	public static String SDCARD_ROOT_PATH = null;
	
	// ����Ϊ���Ÿ�·��
	public final static String ASIM_ROOT_PATH = "/asim"; 
	public final static String CACHE_PATH = ASIM_ROOT_PATH + "/cache";
	public final static String LOG_PATH = ASIM_ROOT_PATH + "/log";
	public final static String DATA_PATH = ASIM_ROOT_PATH + "/data";

	// ����Ϊ�û����µ���Ŀ¼
	public final static String DB_PATH = "/database"; 
	public final static String IMAGE_PATH = "/image";
	public final static String AUDIO_PATH = "/audio";
	public final static String VIDEO_PATH = "/video";
	public final static String FILE_PATH = "/file";
	public final static String TEMP_PATH = "/temp";

	/**
	 * 
	 * ��Դ�ļ������淶��
	 *   ��ͨ��־�ļ���normal-ʱ���.log
	 *   ������־�ļ���crash-ʱ���.log���ϴ���������ʱ���ϻ��ͺͰ汾��Ϣ��Ϊǰ׺������-�汾-(�û���)-crash-ʱ���.log��
	 *   ���ͱ������������ļ����ļ�ԭ��
	 *   �����������շ��͵���Ƭ�������û���-�Է��û���-image-ʱ���.src
	 *   ��������¼Ӱ���͵Ķ���Ƶ�������û���-video-ʱ���.mp4(��׺�� VCamera ���ָ��)
	 *   ��������¼Ӱ���͵Ķ���Ƶ����ͼ�������û���-video-ʱ���.jpg(��׺�� VCamera ���ָ��)
	 *   ���յ����ѷ�������Ƭ���Է��û���-�����û���-image-ʱ���.src
	 *   ���յ����ѷ�������Ƭ����ͼ���Է��û���-�����û���-image-ʱ���.thumb
	 *   ���յ����ѷ�������Ƶ���Է��û���-�����û���-video-ʱ���.src
	 *   ���յ����ѷ�������Ƶ����ͼ���Է��û���-�����û���-video-ʱ���.thumb
	 *   ��������¼�����͵������������û���-�Է��û���-audio-ʱ���.src
	 *   ���յ����ѷ������������Է��û���-�����û���-audio-ʱ���.src
	 *   ���ͺͽ��յ���ͨ�ļ����ļ�ԭ�� 
	 *   
	 */
	
	public final static String IMAGE_PREFIX = "image";
	public final static String VIDEO_PREFIX = "video";
	public final static String AUDIO_PREFIX = "audio";

	public final static String FILE_SUFFIX = ".src";
	public final static String THUMB_SUFFIX = ".thumb";
	public final static String LOG_PREFIX = "normal";
	public final static String CRASH_PREFIX = "crash";
	public final static String SIPLOG_PREFIX = "siplog";

	
	// ���س�ʱʱ�䣨10���ӣ�
	public final static int FILE_DOWNLOAD_TIMEOUT = 1000 * 60 * 10;
	
	// ���ŷ��ͳ�ʱʱ�䣨30�룩
	public final static int SMS_SEND_RESULT_TIMEOUT = 1000 * 30;

	// ���Ͷ�ý���ļ��Ĵ�С���ޣ�10M��
	public final static int SEND_MEDIA_FILE_SIZE_LIMIT = 10 * 1024 * 1024;
	
}
