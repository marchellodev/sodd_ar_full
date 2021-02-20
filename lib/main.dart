import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:location/location.dart' as location;
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(body: Center(child: App())),
    );
  }
}

class App extends StatefulWidget {
  @override
  _AppState createState() => _AppState();
}

class _AppState extends State<App> {
  static const platform = const MethodChannel('samples.flutter.dev/battery');

  String _batteryLevel = 'null';

  Future<void> _getBatteryLevel() async {
    String batteryLevel;
    try {
      final int result = await platform.invokeMethod('getBatteryLevel');
      batteryLevel = 'Battery level at $result % .';
    } on PlatformException catch (e) {
      batteryLevel = "Failed to get battery level: '${e.message}'.";
    }

    setState(() {
      _batteryLevel = batteryLevel;
    });
  }

  Future<void> _open() async {
    var status = await Permission.camera.status;
    if (status.isUndetermined) {
      print('asking for permissions');
      Map<Permission, PermissionStatus> statuses = await [
        Permission.location,
        Permission.storage,
        Permission.camera,
        Permission.location,
        Permission.sensors,
      ].request();

      print(statuses);
    }
    location.Location l = new location.Location();


    var _locationData = await l.getLocation();

    print(_locationData.latitude);
    print(_locationData.longitude);

    String batteryLevel;
    try {
      final int result = await platform.invokeMethod('openAr',
          {"text1": '${_locationData.latitude},${_locationData.longitude}', "text2": '51.607463,24.993685'});
      batteryLevel = 'Battery level at $result % .';
    } on PlatformException catch (e) {
      batteryLevel = "Failed to get battery level: '${e.message}'.";
    }

    setState(() {
      _batteryLevel = batteryLevel;
    });
  }

  @override
  void initState() {
    _getBatteryLevel();

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Container(
          child: Text(_batteryLevel),
        ),
        MaterialButton(
          onPressed: () {
            _open();
          },
          child: Text('open'),
        )
      ],
    );
  }
}
