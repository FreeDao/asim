package com.view.asim.comm;

import android.os.Environment;

public class Constant {
	
	/**
	 * 锟斤拷锟竭硷拷锟斤拷锟斤拷 ACTION 
	 */
	public static final String OTA_CHECK_ACTION = "ota.checknow";

	/**
	 * 锟斤拷锟斤拷锟斤拷锟斤拷删锟斤拷锟斤拷ACTION锟斤拷KEY
	 */
	public static final String ROSTER_DELETED = "roster.deleted";
	public static final String ROSTER_DELETED_KEY = "roster.deleted.key";

	/**
	 * 锟斤拷锟斤拷锟斤拷锟叫革拷锟铰碉拷ACTION锟斤拷KEY
	 */
	public static final String ROSTER_UPDATED = "roster.updated";
	public static final String ROSTER_UPDATED_KEY = "roster.updated.key";

	/**
	 * 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟接碉拷ACTION锟斤拷KEY
	 */
	public static final String ROSTER_ADDED = "roster.added";
	public static final String ROSTER_ADDED_KEY = "roster.added.key";

	/**
	 * 锟斤拷锟斤拷锟斤拷锟叫筹拷员状态锟叫改憋拷锟�ACTION锟斤拷KEY
	 */
	public static final String ROSTER_PRESENCE_CHANGED = "roster.presence.changed";
	public static final String ROSTER_PRESENCE_CHANGED_KEY = "roster.presence.changed.key";

	/**
	 * 锟秸碉拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
	 */
	public static final String ROSTER_SUBSCRIPTION = "roster.subscribe";
	public static final String ROSTER_SUB_FROM = "roster.subscribe.from";
	public static final String NOTICE_ID = "notice.id";

	/**
	 * 锟斤拷锟斤拷息
	 */
	public static final String NEW_MESSAGE_ACTION = "roster.newmessage";
	public static final String NEW_CTRL_MESSAGE_ACTION = "roster.newctrlmessage";
	
	/**
	 * 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷息通知
	 */
	public static final String RECV_OFFLINE_MSG_ACTION = "roster.offlinemessage";

	/**
	 * 通知锟斤拷锟斤拷通知
	 */
	public static final String RECONN_ACTION = "xmpp.reconn";
	
	/**
	 * 锟斤拷锟斤拷锟侥硷拷
	 */
	public static final String SEND_FILE_ACTION = "immessage.sendfile";
	public static final String SEND_FILE_RESULT_ACTION = "immessage.sendfile.result";
	public static final String FILE_PROGRESS_ACTION = "immessage.sendfile.progress";
	public static final String SEND_FILE_KEY_MESSAGE = "immessage.sendfile.key.message";
	public static final String SEND_FILE_KEY_PROGRESS = "immessage.sendfile.key.progress";

	
	/**
	 * 锟斤拷锟斤拷锟斤拷息
	 */
	public static final String SEND_MESSAGE_ACTION = "immessage.sendmessage";
	public static final String SEND_MESSAGE_RESULT_ACTION = "immessage.sendmessage.result";
	public static final String SEND_MESSAGE_KEY_MESSAGE = "immessage.sendmessage.key.message";
	
	/**
	 * 群锟斤拷锟斤拷锟斤拷/锟剿筹拷/锟斤拷锟斤拷
	 */
	public static final String GROUP_INVITE_ACTION = "immessage.group.invite";
	public static final String GROUP_QUIT_ACTION = "immessage.group.quit";
	public static final String GROUP_UPDATE_ACTION = "immessage.group.update";
	public static final String GROUP_ACTION_KEY_INFO = "immessage.group.key.info";
	
	/**
	 * 远锟斤拷锟斤拷锟斤拷锟斤拷息
	 */
	public static final String REMOTE_DESTROY_ACTION = "immessage.remotedestroy";
	public static final String REMOTE_DESTROY_KEY_MESSAGE = "immessage.remotedestroy.key.message";
	
	public static final String DESTROY_RECEIPTS_ACTION = "immessage.destroyreceipts";
	public static final String DESTROY_RECEIPTS_KEY_MESSAGE = "immessage.destroyreceipts.key.message";

	/**
	 * 锟斤拷锟斤拷频锟斤拷锟侥硷拷预锟斤拷
	 */
	public static final String PREVIEW_AUDIO = "preview.audio";
	public static final String PREVIEW_VIDEO = "preview.video";
	public static final String PREVIEW_IMAGE = "preview.image";
	public static final String PREVIEW_FILE  = "preview.file";

	/**
	 * 锟斤拷司锟斤拷状态锟斤拷锟斤拷
	 */
	public static final String AUKEY_STATUS_UPDATE = "aukey.status.update";
	public static final String AUKEY_STATUS_KEY = "aukey.status.key";
	

	/**
	 * 锟揭碉拷锟斤拷息
	 */
	public static final String MY_NEWS = "my.news";
	public static final String MY_NEWS_DATE = "my.news.date";

	/**
	 * IM 锟斤拷锟斤拷
	 */
	public static final String IM_SET_PREF = "asim_im_settings_pref";// 锟斤拷录锟斤拷锟斤拷
	public static final String USERNAME = "username";// 锟剿伙拷
	public static final String PASSWORD = "password";// 锟斤拷锟斤拷
	public static final String XMPP_HOST = "xmpp_host";// 锟斤拷址
	public static final String XMPP_PORT = "xmpp_port";// 锟剿匡拷
	public static final String XMPP_SERVICE_NAME = "xmpp_service_name";// 锟斤拷锟斤拷锟斤拷
	public static final String DATA_ROOT_PATH = "data_root_path"; // 锟斤拷锟捷达拷锟铰凤拷锟斤拷母锟侥柯�
	public static final String APP_UUID = "asim_app_uuid"; 
	//public static final String XMPP_RESOURCE_NAME = "asim";

	/**
	 * SIP 锟斤拷锟斤拷
	 */
	public static final String SIP_SET_PREF = "asim_sip_settings_pref";// 锟斤拷录锟斤拷锟斤拷
	public static final String SIP_HOST = "sip_host";
	public static final String SIP_PORT = "sip_port";
	public static final String STUN_HOST = "stun_host";

	/**
	 * 锟斤拷锟斤拷锟斤拷锟斤拷址
	 */
//	public static final String IM_SERVICE_HOST = "112.124.32.193";
//	public static final int IM_SERVICE_PORT = 5222;
//	public static final String IM_SERVICE_NAME = "112.124.32.193";
//	public static final String VOIP_SERVICE_HOST = "121.40.69.120";
//	public static final int VOIP_SERVICE_PORT = 6060;
//	public static final String VOIP_STUN_SERVER = "121.40.69.120";
	
	
	/**
	 * 连接服务器/账户登录/注册/搜索返回值
	 */
	public static final int SERVER_SUCCESS = 0;// 成功
	public static final int HAS_NEW_VERSION = 1;// 发现新版本
	public static final int IS_NEW_VERSION = 2;// 当前版本为最新
	public static final int LOGIN_ERROR_ACCOUNT_PASS = 3;// 账号或者密码错误
	public static final int LOGIN_ERROR_DUPLICATED = 4;// 重复登录
	public static final int SIGNUP_ERROR_ACCOUNT_PASS = 5;// 账号或者密码错误
	public static final int SERVER_UNAVAILABLE = 6;// 无法连接到服务器
	public static final int UNKNOWN_ERROR = 7; // 未知错误
	public static final int NONE_RESULTS = 8; // 搜索失败错误
	public static final int DISK_FULL_ERROR = 9; // 磁盘已满

	/**
	 * 数据库版本: 
	 * V0.4.28，数据库版本：V1（初始版本）
	 * V0.5.14，数据库版本：V2（修改了 im_notice 表）
	 * V0.6.25，数据库版本：V3（修改了 im_notice 和 im_msg_his 表）
	 * V0.7.15，数据库版本：V4（增加了 SIP 相关表）
	 */
	public static final int DB_VERSION = 4;

	public static final String XMPP_CONNECTION_CLOSED = "xmpp_connection_closed";// 锟斤拷锟斤拷锟叫讹拷

	public static final String LOGIN = "login"; // 锟斤拷录
	public static final String RELOGIN = "relogin"; // 锟斤拷锟铰碉拷录

	/**
	 * 锟斤拷锟斤拷锟叫憋拷 锟斤拷锟斤拷
	 */
	public static final String ALL_FRIEND = "锟斤拷锟叫猴拷锟斤拷";// 锟斤拷锟叫猴拷锟斤拷
	public static final String NO_GROUP_FRIEND = "未锟斤拷锟斤拷锟斤拷锟�";// 锟斤拷锟叫猴拷锟斤拷
	/**
	 * 系统锟斤拷息
	 */
	public static final String ACTION_SYS_MSG = "action_sys_msg";// 锟斤拷息锟斤拷锟酵关硷拷锟斤拷
	public static final String MSG_TYPE = "broadcast";// 锟斤拷息锟斤拷锟酵关硷拷锟斤拷
	public static final String SYS_MSG = "sysMsg";// 系统锟斤拷息锟截硷拷锟斤拷
	public static final String SYS_MSG_DIS = "系统锟斤拷息";// 系统锟斤拷息
	public static final String ADD_FRIEND_QEQUEST = "锟斤拷锟斤拷锟斤拷锟斤拷";// 系统锟斤拷息锟截硷拷锟斤拷
	/**
	 * 锟斤拷锟斤拷某锟斤拷锟斤拷锟斤拷锟斤拷锟截碉拷状态值
	 */
	public static final int SUCCESS = 0;// 锟斤拷锟斤拷
	public static final int FAIL = 1;// 锟斤拷锟斤拷锟斤拷
	public static final int UNKNOWERROR = 2;// 锟斤拷锟斤拷莫锟斤拷锟侥达拷锟斤拷.
	public static final int NETWORKERROR = 3;// 锟斤拷锟斤拷锟斤拷锟�
	/***
	 * 锟斤拷业通讯录锟斤拷锟斤拷锟矫伙拷锟斤拷锟斤拷锟矫伙拷锟斤拷去锟斤拷锟斤拷锟斤拷员锟叫碉拷锟斤拷锟斤拷锟斤拷锟斤拷锟角凤拷锟斤拷锟斤拷锟斤拷锟街�
	 */
	public static final int containsZz = 0;
	/***
	 * 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟较碉拷锟斤拷斜锟�xml锟斤拷页锟斤拷锟斤拷
	 */
	public static final String currentpage = "1";// 锟斤拷前锟节硷拷页
	public static final String pagesize = "1000";// 锟斤拷前页锟斤拷锟斤拷锟斤拷

	/***
	 * 锟斤拷锟斤拷锟斤拷锟斤拷xml锟斤拷锟斤拷锟斤拷锟斤拷
	 */
	public static final String add = "00";// 锟斤拷锟斤拷
	public static final String rename = "01";// 锟斤拷锟斤拷
	public static final String remove = "02";// 锟斤拷锟斤拷

	/**
	 * 锟斤拷锟斤拷锟斤拷
	 */
	/**
	 * 锟斤拷锟斤拷锟斤拷状态action
	 * 
	 */
	public static final String ACTION_RECONNECT_STATE = "action_reconnect_state";
	/**
	 * 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷状态锟侥关伙拷锟接ｏ拷锟侥放碉拷intent锟侥关硷拷锟斤拷
	 */
	public static final String RECONNECT_STATE = "reconnect_state";
	/**
	 * 锟斤拷锟斤拷锟斤拷锟斤拷锟接ｏ拷
	 */
	public static final boolean RECONNECT_STATE_SUCCESS = true;
	public static final boolean RECONNECT_STATE_FAIL = false;
	/**
	 * 锟角凤拷锟斤拷锟竭碉拷SharedPreferences锟斤拷锟斤拷
	 */
	public static final String PREFENCE_USER_STATE = "prefence_user_state";
	public static final String IS_ONLINE = "is_online";
	
	/**
	 * 锟斤拷页锟斤拷注锟斤拷页锟斤拷之锟斤拷慕锟斤拷锟斤拷锟�
	 */
	public static final int SIGNUP_RESULT = 0;
	
	/**
	 * Activity 之锟斤拷锟� requestCode
	 */
	
	// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟较�锟睫改斤拷锟斤拷
	public final static int REQCODE_MOD_REMARK = 0;
	public final static int REQCODE_MOD_LOCATION = 1;
	public final static int REQCODE_MOD_NICKNAME = 2;
	public final static int REQCODE_MOD_AVATAR_BY_GALLERY = 3;
	public final static int REQCODE_MOD_AVATAR_BY_CAPTURE = 4;
	public final static int REQCODE_MOD_AVATAR_CROP = 5;
	public final static int REQCODE_LOGIN_OP = 6;
	
	// 锟斤拷锟斤拷锟斤拷锟斤拷 intent action
	public final static int REQCODE_IMAGE_PICK = 10;
	public final static int REQCODE_VIDEO_PICK = 11;
	public final static int REQCODE_TAKE_PICTURE = 12;
	public final static int REQCODE_FILE_PICK = 13;
	public final static int REQCODE_CAPTURE_VIDEO = 14;

	// 锟斤拷锟斤拷锟斤拷锟斤拷选锟斤拷锟斤拷系锟剿斤拷锟斤拷
	public final static int REQCODE_SELECT_USERS = 20;
	
	// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷暮蠹捶俳锟斤拷锟斤拷锟� action
	public final static int REQCODE_BURN_AFTER_READ = 30;

	// IM 锟斤拷息锟斤拷源锟斤拷
	public final static String FILE_STORAGE_HOST = "com-viewiot-mobile-asim.qiniudn.com";
	
	// App 锟斤拷志锟斤拷
	public final static String DUMPS_STORAGE_HOST = "com-viewiot-mobile-asim-dumps.qiniudn.com";

	// App OTA锟斤拷
	public final static String OTA_STORAGE_HOST = "com-viewiot-mobile-asim-ota.qiniudn.com";
	public final static String OTA_CHECK_STATUS_PATH = "/upgrade_rule.json";
	
	// App Discovery Server
	public final static String MAIN_DISC_SERVER_URL = "http://service.akey.im/get_asim_server.php";
	public final static String SLAVE_DISC_SERVER_URL = "http://com-viewiot-mobile-asim-ota.qiniudn.com/get_asim_server.json";
	

	
	/**
	 * 锟斤拷锟捷伙拷锟斤拷目录锟结构
	 *       asim
	 *         |
	 *         |--- cache (锟斤拷锟斤拷频也锟捷达拷锟节达拷)
	 *         |--- log
	 *         |--- data
	 *                |--- 锟矫伙拷 A
	 *                |      |
	 *                |      |--- database
	 *                |      |--- temp
	 *                |      |--- image
	 *                |      |      |
	 *                |      |      |--- 锟斤拷锟斤拷 A
	 *                |      |      |--- 锟斤拷锟斤拷 B
	 *                |      |--- audio
	 *                |      |      |
	 *                |      |      |--- 锟斤拷锟斤拷 A
	 *                |      |      |--- 锟斤拷锟斤拷 B
	 *                |      |--- video
	 *                |      |      |
	 *                |      |      |--- 锟斤拷锟斤拷 A
	 *                |      |      |--- 锟斤拷锟斤拷 B
	 *                |      |--- file
	 *                |             |
	 *                |             |--- 锟斤拷锟斤拷 A
	 *                |             |--- 锟斤拷锟斤拷 B
	 *                |--- 锟矫伙拷 B
	 */
	
	// 锟斤拷锟捷达拷锟斤拷诖娲�锟斤拷锟斤拷锟斤拷锟铰凤拷锟斤拷锟斤拷锟�
	public static String SDCARD_ROOT_PATH = null;
	
	// 锟斤拷锟斤拷为锟斤拷锟脚革拷路锟斤拷
	public final static String ASIM_ROOT_PATH = "/asim"; 
	public final static String CACHE_PATH = ASIM_ROOT_PATH + "/cache";
	public final static String LOG_PATH = ASIM_ROOT_PATH + "/log";
	public final static String DATA_PATH = ASIM_ROOT_PATH + "/data";

	// 锟斤拷锟斤拷为锟矫伙拷锟斤拷锟铰碉拷锟斤拷目录
	public final static String DB_PATH = "/database"; 
	public final static String IMAGE_PATH = "/image";
	public final static String AUDIO_PATH = "/audio";
	public final static String VIDEO_PATH = "/video";
	public final static String FILE_PATH = "/file";
	public final static String TEMP_PATH = "/temp";

	/**
	 * 
	 * 锟斤拷源锟侥硷拷锟斤拷锟斤拷锟芥范锟斤拷
	 *   锟斤拷通锟斤拷志锟侥硷拷锟斤拷normal-时锟斤拷锟�.log
	 *   锟斤拷锟斤拷锟斤拷志锟侥硷拷锟斤拷crash-时锟斤拷锟�.log锟斤拷锟较达拷锟斤拷锟斤拷锟斤拷锟斤拷时锟斤拷锟较伙拷锟酵和版本锟斤拷息锟斤拷为前缀锟斤拷锟斤拷锟斤拷-锟芥本-(锟矫伙拷锟斤拷)-crash-时锟斤拷锟�.log锟斤拷
	 *   锟斤拷锟酵憋拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟侥硷拷锟斤拷锟侥硷拷原锟斤拷
	 *   锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟秸凤拷锟酵碉拷锟斤拷片锟斤拷锟斤拷锟斤拷锟矫伙拷锟斤拷-锟皆凤拷锟矫伙拷锟斤拷-image-时锟斤拷锟�.src
	 *   锟斤拷锟斤拷锟斤拷锟斤拷录影锟斤拷锟酵的讹拷锟斤拷频锟斤拷锟斤拷锟斤拷锟矫伙拷锟斤拷-video-时锟斤拷锟�.mp4(锟斤拷缀锟斤拷 VCamera 锟斤拷锟街革拷锟�)
	 *   锟斤拷锟斤拷锟斤拷锟斤拷录影锟斤拷锟酵的讹拷锟斤拷频锟斤拷锟斤拷图锟斤拷锟斤拷锟斤拷锟矫伙拷锟斤拷-video-时锟斤拷锟�.jpg(锟斤拷缀锟斤拷 VCamera 锟斤拷锟街革拷锟�)
	 *   锟斤拷锟秸碉拷锟斤拷锟窖凤拷锟斤拷锟斤拷锟斤拷片锟斤拷锟皆凤拷锟矫伙拷锟斤拷-锟斤拷锟斤拷锟矫伙拷锟斤拷-image-时锟斤拷锟�.src
	 *   锟斤拷锟秸碉拷锟斤拷锟窖凤拷锟斤拷锟斤拷锟斤拷片锟斤拷锟斤拷图锟斤拷锟皆凤拷锟矫伙拷锟斤拷-锟斤拷锟斤拷锟矫伙拷锟斤拷-image-时锟斤拷锟�.thumb
	 *   锟斤拷锟秸碉拷锟斤拷锟窖凤拷锟斤拷锟斤拷锟斤拷频锟斤拷锟皆凤拷锟矫伙拷锟斤拷-锟斤拷锟斤拷锟矫伙拷锟斤拷-video-时锟斤拷锟�.src
	 *   锟斤拷锟秸碉拷锟斤拷锟窖凤拷锟斤拷锟斤拷锟斤拷频锟斤拷锟斤拷图锟斤拷锟皆凤拷锟矫伙拷锟斤拷-锟斤拷锟斤拷锟矫伙拷锟斤拷-video-时锟斤拷锟�.thumb
	 *   锟斤拷锟斤拷锟斤拷锟斤拷录锟斤拷锟斤拷锟酵碉拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟矫伙拷锟斤拷-锟皆凤拷锟矫伙拷锟斤拷-audio-时锟斤拷锟�.src
	 *   锟斤拷锟秸碉拷锟斤拷锟窖凤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟皆凤拷锟矫伙拷锟斤拷-锟斤拷锟斤拷锟矫伙拷锟斤拷-audio-时锟斤拷锟�.src
	 *   锟斤拷锟酵和斤拷锟秸碉拷锟斤拷通锟侥硷拷锟斤拷锟侥硷拷原锟斤拷 
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

	
	// 锟斤拷锟截筹拷时时锟戒（10锟斤拷锟接ｏ拷
	public final static int FILE_DOWNLOAD_TIMEOUT = 1000 * 60 * 10;
	
	// 锟斤拷锟脚凤拷锟酵筹拷时时锟戒（30锟诫）
	public final static int SMS_SEND_RESULT_TIMEOUT = 1000 * 30;
	
	// 锟斤拷锟斤拷Discovery Server 锟斤拷时时锟戒（30锟诫）
	public final static int DISCOVERY_TIMEOUT = 1000 * 30;

	// 锟斤拷锟酵讹拷媒锟斤拷锟侥硷拷锟侥达拷小锟斤拷锟睫ｏ拷10M锟斤拷
	public final static int SEND_MEDIA_FILE_SIZE_LIMIT = 10 * 1024 * 1024;
	
	
	// AVOSCloud 锟狡凤拷锟斤拷锟斤拷
	public final static String EVENT_TAG_IM_SEND_TEXT_PLAIN = "im_send_text_plain";
	public final static String EVENT_TAG_IM_SEND_TEXT_ENCRYPTED = "im_send_text_encrypted";
	public final static String EVENT_TAG_IM_SEND_PIC_PLAIN = "im_send_pic_plain";
	public final static String EVENT_TAG_IM_SEND_PIC_ENCRYPTED = "im_send_pic_encrypted";
	public final static String EVENT_TAG_IM_SEND_AUDIO_PLAIN = "im_send_audio_plain";
	public final static String EVENT_TAG_IM_SEND_AUDIO_ENCRYPTED = "im_send_audio_encrypted";
	public final static String EVENT_TAG_IM_SEND_VIDEO_PLAIN = "im_send_video_plain";
	public final static String EVENT_TAG_IM_SEND_VIDEO_ENCRYPTED = "im_send_video_encrypted";

	public final static String EVENT_TAG_VOIP_OUTGOING_CALL_PLAIN = "voip_outgoing_call_plain";
	public final static String EVENT_TAG_VOIP_OUTGOING_CALL_ENCRYPTED = "voip_outgoing_call_encrypted";
	public final static String EVENT_TAG_VOIP_INCOMING_CALL_PLAIN = "voip_incoming_call_plain";
	public final static String EVENT_TAG_VOIP_INCOMING_CALL_ENCRYPTED = "voip_incoming_call_encrypted";
	
}
