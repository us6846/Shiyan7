# Shiyan7
掌握Broadcast Receiver的使用  
接收全局和局部广播  
发送全局和局部广播  
Broadcast Receiver监测电量与充电状态 
Broadcast Receiver判断并监测网络连接状态

1.Broadcast Receiver监测充电状态的改变
当你想通过改变后台更新操作的频率来减少对电池寿命的影响时，那么首先需要检查当前电量与充电状态。
执行应用更新对电池寿命的影响是与电量和充电状态密切相关的。当使用交流电对设备充电时，更新操作的影响可以忽略不计，所以在大多数情况下，如果使用壁式充电器对设备进行充电，我们可以将刷新频率设置到最大。相反的，如果设备没有在充电状态，那么我们就需要尽量减少设备的更新操作来延长电池的续航能力。同样的，如果我们监测到电量即将耗尽时，那么应该尽可能降低甚至停止更新操作。
判断当前充电状态
首先来看一下应该如何确定当前的充电状态。BatteryManager会广播一个带有电池与充电详情的Sticky Intent
因为广播的是一个sticky Intent，所以不需要注册BroadcastReceiver。仅仅只需要调用一个以null作为Receiver参数的registerReceiver()方法就可以了。如下面的代码片段中展示的那样，它返回了保存当前电池信息的Intent。你也可以在这里传入一个实际的BroadcastReceiver对象，但这并不是必须的。
IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
Intent batteryStatus = context.registerReceiver(null, ifilter);
我们可以提取出当前的充电状态，以及设备处于充电时，是通过USB还是交流充电器充电的。
// Are we charging / charged?
int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                     status == BatteryManager.BATTERY_STATUS_FULL;

// How are we charging?
int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
通常，我们可以在设备使用交流充电时最大化后台更新频率，在使用USB充电时降低更新频率，在非充电状态时，将更新频率进一步降低。
监测充电状态的改变
充电状态随时可能改变，所以我们应该检查充电状态的改变来调整更新频率。BatteryManager会在设备连接或者断开充电器的时候广播一个Action。即使应用没有运行，我们也应该接收这些事件的广播，主要原因是因为这些事件会影响到应用启动（从而进行更新）的频率，因此我们应该在Manifest文件里面注册一个BroadcastReceiver来监听含有ACTION_POWER_CONNECTED 与ACTION_POWER_DISCONNECTED的Intent。
<receiver android:name=".PowerConnectionReceiver">
  <intent-filter>
    <action android:name="android.intent.action.ACTION_POWER_CONNECTED"/>
    <action android:name="android.intent.action.ACTION_POWER_DISCONNECTED"/>
  </intent-filter>
</receiver>
我们可以在该BroadcastReceiver的实现中，提取出当前的充电状态，如下所示：
public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
    }
}
判断当前电池电量
在一些情况下，获取到当前电池电量也很有帮助。我们可以在获知电量少于某个级别的时候减少后台的更新频率。 我们可以通过电池状态Intent获取到电池电量与容量等信息，如下所示：
int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

float batteryPct = level / (float)scale;
检测电量的有效改变
我们不能不停地监测电池状态，实际上这也是不必要的。通常来说，不间断地监测电量信息对电池的影响会远大于应用本身对电池的影响。所以我们应该仅监测电量的一些显著性变化，特别是当设备进入或者离开低电量状态时。
在下面的Manifest文件片段中，BroadcastReceiver仅仅监听ACTION_BATTERY_LOW与ACTION_BATTERY_OKAY，这样它就只会在设备电量进入低电量或者离开低电量的时候被触发。
<receiver android:name=".BatteryLevelReceiver">
<intent-filter>
  <action android:name="android.intent.action.ACTION_BATTERY_LOW"/>
  <action android:name="android.intent.action.ACTION_BATTERY_OKAY"/>
  </intent-filter>
</receiver>
通常我们都需要在进入低电量的情况下，关闭所有后台更新来维持设备的续航，因为这个时候做任何更新等操作都极有可能是无用的，因为也许在你还没来得及处理更新的数据时，设备就因电量耗尽而自动关机了。
创建一个项目，完成app的监测电量与充电状态，当检测到处于充电状态是打印到logcat显示正在充电。当电量低和离开电量低时打印电量信息到logcat。（提交Receiver类代码）
实验截图：

2.Broadcast Receiver判断并监测网络连接状态
判断当前是否有网络连接
如果没有网络连接，那么就没有必要做那些需要联网的事情。下面的代码片段展示了如何通过ConnectivityManager检查当前活动的网络类型，并确定它是否可以连接到互联网：
ConnectivityManager cm =
        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
boolean isConnected = activeNetwork != null &&
                      activeNetwork.isConnectedOrConnecting();

判断连接网络的类型
我们还可以获取到当前的网络连接类型。
设备通常可以有移动网络，WiMax，Wi-Fi与以太网连接等类型。通过查询当前活动的网络类型，可以根据网络的带宽对更新频率进行调整：
boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
移动网络的使用费会比Wi-Fi更高，所以多数情况下，如果设备正在使用移动网络，我们应该减少应用的更新频率；同样地，还应该临时地挂起一些文件下载任务直到有Wi-Fi连接时再继续下载。
如果已经关闭了更新操作，那么需要监听网络连接的变化，这样就可以在建立了互联网访问之后，重新恢复它们。
监听网络连接的变化
当网络连接发生改变时，ConnectivityManager会广CONNECTIVITY_ACTION（android.net.conn.CONNECTIVITY_CHANGE）的Action消息。 我们可以在Manifest文件里面注册一个BroadcastReceiver，来监听这些变化，并适当地恢复（或挂起）你的后台更新:
<action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
设备的网络变化可能会比较频繁，因此每当你在移动网络与Wi-Fi之间切换的时候，这一广播就会被触发。
请创建项目，监听网络的变化，当网络连接时打印网络的类型到logcat。当网络断开时打印连接断开。（提交Receiver类代码）

实验截图：


链接：https://github.com/us6846/Shiyan7
