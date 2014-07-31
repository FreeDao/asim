package com.view.asim.comm;

import android.os.Environment;

public class Constant {
	
	/**
	 * 在线检查更新 ACTION 
	 */
	public static final String OTA_CHECK_ACTION = "ota.checknow";

	/**
	 * 花名册有删除的ACTION和KEY
	 */
	public static final String ROSTER_DELETED = "roster.deleted";
	public static final String ROSTER_DELETED_KEY = "roster.deleted.key";

	/**
	 * 花名册有更新的ACTION和KEY
	 */
	public static final String ROSTER_UPDATED = "roster.updated";
	public static final String ROSTER_UPDATED_KEY = "roster.updated.key";

	/**
	 * 花名册有增加的ACTION和KEY
	 */
	public static final String ROSTER_ADDED = "roster.added";
	public static final String ROSTER_ADDED_KEY = "roster.added.key";

	/**
	 * 花名册中成员状态有改变的ACTION和KEY
	 */
	public static final String ROSTER_PRESENCE_CHANGED = "roster.presence.changed";
	public static final String ROSTER_PRESENCE_CHANGED_KEY = "roster.presence.changed.key";

	/**
	 * 收到好友邀请请求
	 */
	public static final String ROSTER_SUBSCRIPTION = "roster.subscribe";
	public static final String ROSTER_SUB_FROM = "roster.subscribe.from";
	public static final String NOTICE_ID = "notice.id";

	/**
	 * 新消息
	 */
	public static final String NEW_MESSAGE_ACTION = "roster.newmessage";
	public static final String NEW_CTRL_MESSAGE_ACTION = "roster.newctrlmessage";
	
	/**
	 * 接收离线消息通知
	 */
	public static final String RECV_OFFLINE_MSG_ACTION = "roster.offlinemessage";

	/**
	 * 通知重连通知
	 */
	public static final String RECONN_ACTION = "xmpp.reconn";
	
	/**
	 * 发送文件
	 */
	public static final String SEND_FILE_ACTION = "immessage.sendfile";
	public static final String SEND_FILE_RESULT_ACTION = "immessage.sendfile.result";
	public static final String FILE_PROGRESS_ACTION = "immessage.sendfile.progress";
	public static final String SEND_FILE_KEY_MESSAGE = "immessage.sendfile.key.message";
	public static final String SEND_FILE_KEY_PROGRESS = "immessage.sendfile.key.progress";

	
	/**
	 * 发送消息
	 */
	public static final String SEND_MESSAGE_ACTION = "immessage.sendmessage";
	public static final String SEND_MESSAGE_RESULT_ACTION = "immessage.sendmessage.result";
	public static final String SEND_MESSAGE_KEY_MESSAGE = "immessage.sendmessage.key.message";
	
	/**
	 * 群组邀请/退出/更新
	 */
	public static final String GROUP_INVITE_ACTION = "immessage.group.invite";
	public static final String GROUP_QUIT_ACTION = "immessage.group.quit";
	public static final String GROUP_UPDATE_ACTION = "immessage.group.update";
	public static final String GROUP_ACTION_KEY_INFO = "immessage.group.key.info";
	
	/**
	 * 远程销毁消息
	 */
	public static final String REMOTE_DESTROY_ACTION = "immessage.remotedestroy";
	public static final String REMOTE_DESTROY_KEY_MESSAGE = "immessage.remotedestroy.key.message";

	/**
	 * 音视频和文件预览
	 */
	public static final String PREVIEW_AUDIO = "preview.audio";
	public static final String PREVIEW_VIDEO = "preview.video";
	public static final String PREVIEW_IMAGE = "preview.image";
	public static final String PREVIEW_FILE  = "preview.file";

	/**
	 * 安司盾状态更新
	 */
	public static final String AUKEY_STATUS_UPDATE = "aukey.status.update";
	public static final String AUKEY_STATUS_KEY = "aukey.status.key";
	

	/**
	 * 我的消息
	 */
	public static final String MY_NEWS = "my.news";
	public static final String MY_NEWS_DATE = "my.news.date";

	/**
	 * IM 配置
	 */
	public static final String IM_SET_PREF = "asim_im_settings_pref";// 登录设置
	public static final String USERNAME = "username";// 账户
	public static final String PASSWORD = "password";// 密码
	public static final String XMPP_HOST = "xmpp_host";// 地址
	public static final String XMPP_PORT = "xmpp_port";// 端口
	public static final String XMPP_SERVICE_NAME = "xmpp_service_name";// 服务名
	public static final String DATA_ROOT_PATH = "data_root_path"; // 数据存放路径的根目录
	public static final String XMPP_RESOURCE_NAME = "asim";

	/**
	 * SIP 配置
	 */
	public static final String SIP_SET_PREF = "asim_sip_settings_pref";// 登录设置

	/**
	 * 服务器地址
	 */
	public static final String IM_SERVICE_HOST = "112.124.32.193";
	public static final int IM_SERVICE_PORT = 5222;
	public static final String IM_SERVICE_NAME = "112.124.32.193";
	public static final String VOIP_SERVICE_HOST = "121.40.69.120";
	public static final int VOIP_SERVICE_PORT = 6060;
	public static final String VOIP_STUN_SERVER = "121.40.69.120";
	
	
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
	public static final int UNKNOWN_ERROR = 7;// 未知错误
	public static final int NONE_RESULTS = 8;// 搜索失败错误
	
	/**
	 * 数据库版本: 
	 * V0.4.28，数据库版本：V1（初始版本）
	 * V0.5.14，数据库版本：V2（修改了 im_notice 表）
	 * V0.6.25，数据库版本：V3（修改了 im_notice 和 im_msg_his 表）
	 * V0.7.15，数据库版本：V4（增加了 SIP 相关表）
	 */
	public static final int DB_VERSION = 4;

	public static final String XMPP_CONNECTION_CLOSED = "xmpp_connection_closed";// 连接中断

	public static final String LOGIN = "login"; // 登录
	public static final String RELOGIN = "relogin"; // 重新登录

	/**
	 * 好友列表 组名
	 */
	public static final String ALL_FRIEND = "所有好友";// 所有好友
	public static final String NO_GROUP_FRIEND = "未分组好友";// 所有好友
	/**
	 * 系统消息
	 */
	public static final String ACTION_SYS_MSG = "action_sys_msg";// 消息类型关键字
	public static final String MSG_TYPE = "broadcast";// 消息类型关键字
	public static final String SYS_MSG = "sysMsg";// 系统消息关键字
	public static final String SYS_MSG_DIS = "系统消息";// 系统消息
	public static final String ADD_FRIEND_QEQUEST = "好友请求";// 系统消息关键字
	/**
	 * 请求某个操作返回的状态值
	 */
	public static final int SUCCESS = 0;// 存在
	public static final int FAIL = 1;// 不存在
	public static final int UNKNOWERROR = 2;// 出现莫名的错误.
	public static final int NETWORKERROR = 3;// 网络错误
	/***
	 * 企业通讯录根据用户ｉｄ和用户名去查找人员中的请求ｘｍｌ是否包含自组织
	 */
	public static final int containsZz = 0;
	/***
	 * 创建请求分组联系人列表xml分页参数
	 */
	public static final String currentpage = "1";// 当前第几页
	public static final String pagesize = "1000";// 当前页的条数

	/***
	 * 创建请求xml操作类型
	 */
	public static final String add = "00";// 增加
	public static final String rename = "01";// 增加
	public static final String remove = "02";// 增加

	/**
	 * 重连接
	 */
	/**
	 * 重连接状态action
	 * 
	 */
	public static final String ACTION_RECONNECT_STATE = "action_reconnect_state";
	/**
	 * 描述冲连接状态的关机子，寄放的intent的关键字
	 */
	public static final String RECONNECT_STATE = "reconnect_state";
	/**
	 * 描述冲连接，
	 */
	public static final boolean RECONNECT_STATE_SUCCESS = true;
	public static final boolean RECONNECT_STATE_FAIL = false;
	/**
	 * 是否在线的SharedPreferences名称
	 */
	public static final String PREFENCE_USER_STATE = "prefence_user_state";
	public static final String IS_ONLINE = "is_online";
	
	/**
	 * 首页和注册页面之间的交互码
	 */
	public static final int SIGNUP_RESULT = 0;
	
	/**
	 * Activity 之间的 requestCode
	 */
	
	// 主界面与个人信息修改界面
	public final static int REQCODE_MOD_REMARK = 0;
	public final static int REQCODE_MOD_LOCATION = 1;
	public final static int REQCODE_MOD_NICKNAME = 2;
	public final static int REQCODE_MOD_AVATAR_BY_GALLERY = 3;
	public final static int REQCODE_MOD_AVATAR_BY_CAPTURE = 4;
	public final static int REQCODE_MOD_AVATAR_CROP = 5;
	
	// 聊天界面的 intent action
	public final static int REQCODE_IMAGE_PICK = 10;
	public final static int REQCODE_VIDEO_PICK = 11;
	public final static int REQCODE_TAKE_PICTURE = 12;
	public final static int REQCODE_FILE_PICK = 13;
	public final static int REQCODE_CAPTURE_VIDEO = 14;

	// 主界面与选择联系人界面
	public final static int REQCODE_SELECT_USERS = 20;
	
	// 聊天界面与阅后即焚界面间的 action
	public final static int REQCODE_BURN_AFTER_READ = 30;

	// IM 消息资源库
	public final static String FILE_STORAGE_HOST = "com-viewiot-mobile-asim.qiniudn.com";
	
	// App 日志库
	public final static String DUMPS_STORAGE_HOST = "com-viewiot-mobile-asim-dumps.qiniudn.com";

	// App OTA库
	public final static String OTA_STORAGE_HOST = "com-viewiot-mobile-asim-ota.qiniudn.com";
	public final static String OTA_CHECK_STATUS_PATH = "/upgrade_rule.json";

	
	/**
	 * 数据缓存目录结构
	 *       asim
	 *         |
	 *         |--- cache (短视频也暂存在此)
	 *         |--- log
	 *         |--- data
	 *                |--- 用户 A
	 *                |      |
	 *                |      |--- database
	 *                |      |--- temp
	 *                |      |--- image
	 *                |      |      |
	 *                |      |      |--- 好友 A
	 *                |      |      |--- 好友 B
	 *                |      |--- audio
	 *                |      |      |
	 *                |      |      |--- 好友 A
	 *                |      |      |--- 好友 B
	 *                |      |--- video
	 *                |      |      |
	 *                |      |      |--- 好友 A
	 *                |      |      |--- 好友 B
	 *                |      |--- file
	 *                |             |
	 *                |             |--- 好友 A
	 *                |             |--- 好友 B
	 *                |--- 用户 B
	 */
	
	// 数据存放在存储卡的相关路径定义
	public static String SDCARD_ROOT_PATH = null;
	
	// 以下为密信根路径
	public final static String ASIM_ROOT_PATH = "/asim"; 
	public final static String CACHE_PATH = ASIM_ROOT_PATH + "/cache";
	public final static String LOG_PATH = ASIM_ROOT_PATH + "/log";
	public final static String DATA_PATH = ASIM_ROOT_PATH + "/data";

	// 以下为用户名下的子目录
	public final static String DB_PATH = "/database"; 
	public final static String IMAGE_PATH = "/image";
	public final static String AUDIO_PATH = "/audio";
	public final static String VIDEO_PATH = "/video";
	public final static String FILE_PATH = "/file";
	public final static String TEMP_PATH = "/temp";

	/**
	 * 
	 * 资源文件命名规范：
	 *   普通日志文件：normal-时间戳.log
	 *   崩溃日志文件：crash-时间戳.log（上传到服务器时加上机型和版本信息作为前缀，机型-版本-(用户名)-crash-时间戳.log）
	 *   发送本地相册的已有文件：文件原名
	 *   在密信中拍照发送的照片：自身用户名-对方用户名-image-时间戳.src
	 *   在密信中录影发送的短视频：自身用户名-video-时间戳.mp4(后缀由 VCamera 组件指定)
	 *   在密信中录影发送的短视频缩略图：自身用户名-video-时间戳.jpg(后缀由 VCamera 组件指定)
	 *   接收到好友发来的照片：对方用户名-自身用户名-image-时间戳.src
	 *   接收到好友发来的照片缩略图：对方用户名-自身用户名-image-时间戳.thumb
	 *   接收到好友发来的视频：对方用户名-自身用户名-video-时间戳.src
	 *   接收到好友发来的视频缩略图：对方用户名-自身用户名-video-时间戳.thumb
	 *   在密信中录音发送的语音：自身用户名-对方用户名-audio-时间戳.src
	 *   接收到好友发来的语音：对方用户名-自身用户名-audio-时间戳.src
	 *   发送和接收的普通文件：文件原名 
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

	
	// 下载超时时间（10分钟）
	public final static int FILE_DOWNLOAD_TIMEOUT = 1000 * 60 * 10;
	
	// 短信发送超时时间（30秒）
	public final static int SMS_SEND_RESULT_TIMEOUT = 1000 * 30;

	// 发送多媒体文件的大小上限（10M）
	public final static int SEND_MEDIA_FILE_SIZE_LIMIT = 10 * 1024 * 1024;
	
}
