package com.view.asim.comm;

import android.os.Environment;

public class Constant {
	/**
	 * 所有的action的监听的必须要以"ACTION_"开头
	 * 
	 */

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

	/**
	 * 我的消息
	 */
	public static final String MY_NEWS = "my.news";
	public static final String MY_NEWS_DATE = "my.news.date";

	/**
	 * 服务器的配置
	 */
	public static final String LOGIN_SET = "eim_login_set";// 登录设置
	public static final String USERNAME = "username";// 账户
	public static final String PASSWORD = "password";// 密码
	public static final String XMPP_HOST = "xmpp_host";// 地址
	public static final String XMPP_PORT = "xmpp_port";// 端口
	public static final String XMPP_SERVICE_NAME = "xmpp_service_name";// 服务名
	public static final String DATA_ROOT_PATH = "data_root_path"; // 数据存放路径的根目录

	/**
	 * 连接服务器/账户登录/注册/搜索返回值
	 */
	public static final int SERVER_SUCCESS = 0;// 成功
	public static final int HAS_NEW_VERSION = 1;// 发现新版本
	public static final int IS_NEW_VERSION = 2;// 当前版本为最新
	public static final int LOGIN_ERROR_ACCOUNT_PASS = 3;// 账号或者密码错误
	public static final int SIGNUP_ERROR_ACCOUNT_PASS = 4;// 账号或者密码错误
	public static final int SERVER_UNAVAILABLE = 5;// 无法连接到服务器
	public static final int UNKNOWN_ERROR = 6;// 未知错误
	public static final int NONE_RESULTS = 7;// 搜索失败错误

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
	 * 重连接状态acttion
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

	// 主界面与选择联系人界面
	public final static int REQCODE_SELECT_USERS = 20;
	
	// 聊天界面与阅后即焚界面间的 action
	public final static int REQCODE_BURN_AFTER_READ = 30;

	// 图床域名
	public final static String FILE_STORAGE_HOST = "com-viewiot-mobile-asim.qiniudn.com";
	public final static String DUMPS_STORAGE_HOST = "com-viewiot-mobile-asim-dumps.qiniudn.com";
	
	// 数据存放在存储卡的相关路径定义
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
	
	// 下载超时时间（10分钟）
	public final static int FILE_DOWNLOAD_TIMEOUT = 1000 * 60 * 10;

	
}
