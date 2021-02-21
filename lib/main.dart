import 'dart:convert';
import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:latlong/latlong.dart';
import 'package:location/location.dart' as location;
import 'package:permission_handler/permission_handler.dart';
import 'package:polyline/polyline.dart' as p;
import 'package:shared_preferences/shared_preferences.dart';

void main(List<String> arguments) {
  // print('dddddddddddddata');
  // print(data ?? 'init');
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      initialRoute: '/',
      routes: {
        '/': (context) => AppWrapper(),
        '/cam': (context) => CamStuff(),
      },
      title: 'DrivæR',
      theme: ThemeData(
          // primarySwatch: Colors.blue,
          ),
    );
  }
}

class CamStuff extends StatefulWidget {
  @override
  _CamStuffState createState() => _CamStuffState();
}

class _CamStuffState extends State<CamStuff> {
  final mapController = MapController();

  var path = <LatLng>[];

  // Future<void> getL() async {
  //   location.Location l = new location.Location();
  //
  //   var _locationData = await l.getLocation();
  //   mapController.move(
  //       LatLng(_locationData.latitude, _locationData.longitude), 16);
  //
  //   print(_locationData);
  // }

  void getData() async {
    await Future.delayed(Duration(milliseconds: 400));

    // print(await platform.)
    print('preffffffffffffffffs');

    SharedPreferences prefs = await SharedPreferences.getInstance();
    print('affffffffasdasd');

    // LatLng l = LatLng(double.parse(prefs.getString('to').split(',')[0]),
    //     double.parse(prefs.getString('to').split(',')[1]));

    final ar = prefs.getString('from').split(',');
    print(ar);
    print(prefs.getString('from'));
    mapController.move(LatLng(double.parse(ar[0]), double.parse(ar[1])), 16);

    // prefs.setString("points",
    //     '${jsonEncode(path.map((e) => '${e.latitude},${e.longitude}').toList())}');
    final arr = <LatLng>[];
    print(prefs.getString('points'));

    final data = jsonDecode(prefs.getString('points')) as List;
    for (final el in data) {
      arr.add(LatLng(
          double.parse(el.split(',')[0]), double.parse(el.split(',')[1])));
    }
    print('arrrrrrrrrr');
    print(arr);
    setState(() {
      path = arr;
    });
    print('tooooooooooooooooo');
  }

  @override
  void initState() {
    getData();

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    if(path.isEmpty){
      getData();
    }
    return Scaffold(
        backgroundColor: Colors.transparent,
        body: Align(
            alignment: Alignment.bottomCenter,
            child: Opacity(
              opacity: 0.6,
              child: Container(
                height: 240,
                decoration:
                    BoxDecoration(borderRadius: BorderRadius.circular(28)),
                margin: EdgeInsets.only(bottom: 22, right: 22, left: 22),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(28),
                  child: Stack(
                    children: [
                      FlutterMap(
                          mapController: mapController,
                          options: MapOptions(
                            center: LatLng(50.443513, 30.540421),
                            zoom: 16,
                          ),
                          layers: [
                            TileLayerOptions(
                              urlTemplate:
                                  "https://cartodb-basemaps-{s}.global.ssl.fastly.net/light_all/{z}/{x}/{y}.png",
                              subdomains: ['a', 'b', 'c'],
                            ),
                            // MarkerLayerOptions(
                            //   // rebuild: rebuildUserLocation.stream,
                            //   markers: [
                            //     Marker(
                            //       width: 80.0,
                            //       height: 80.0,
                            //       point: pointLocation,
                            //       builder: (ctx) => Container(
                            //         child: Icon(
                            //           Icons.location_on,
                            //           size: 32,
                            //           color: Colors.blue.shade700,
                            //         ),
                            //       ),
                            //     ),
                            //   ],
                            // ),
                            PolylineLayerOptions(
                                // rebuild: pathController.stream,
                                key: UniqueKey(),
                                polylines: [
                                  Polyline(
                                    points: path,
                                    isDotted: true,
                                    color: Colors.blueGrey.shade900,
                                    strokeWidth: 4,
                                  )
                                ])
                          ]),
                      Container(
                        width: MediaQuery.of(context).size.width / 3,
                        height: double.infinity,
                        decoration: BoxDecoration(
                            gradient: LinearGradient(
                                begin: Alignment.centerLeft,
                                end: Alignment.centerRight,
                                colors: [
                              Colors.grey.shade700.withOpacity(1),
                              Colors.grey.shade600.withOpacity(0.8)
                            ])),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              'Колір',
                              style: GoogleFonts.rubik(
                                  color: Colors.white,
                                  fontSize: 14,
                                  fontWeight: FontWeight.w300),
                            ),
                            SizedBox(
                              height: 6,
                            ),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Container(
                                  width: 20,
                                  height: 14,
                                  decoration: BoxDecoration(
                                      color: Colors.red,
                                      borderRadius: BorderRadius.circular(4),
                                      border: Border.all(
                                          color: Colors.white
                                            ..withOpacity(0.2))),
                                ),
                                SizedBox(
                                  width: 8,
                                ),
                                Text(
                                  'Червоний',
                                  style: GoogleFonts.rubik(
                                      color: Colors.white, fontSize: 16),
                                ),
                              ],
                            ),
                            SizedBox(height: 18),
                            Text(
                              'Марка',
                              style: GoogleFonts.rubik(
                                  color: Colors.white,
                                  fontSize: 14,
                                  fontWeight: FontWeight.w300),
                            ),
                            SizedBox(
                              height: 6,
                            ),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Container(
                                  width: 28,
                                  height: 20,
                                  decoration: BoxDecoration(
                                      // color: Colors.grey,
                                      borderRadius: BorderRadius.circular(4),
                                      image: DecorationImage(
                                          image:
                                              AssetImage('assets/audi.png'))),
                                ),
                                SizedBox(
                                  width: 8,
                                ),
                                Text(
                                  'Audi Q3',
                                  style: GoogleFonts.rubik(
                                      color: Colors.white, fontSize: 16),
                                ),
                              ],
                            ),
                            SizedBox(height: 18),
                            Padding(
                              padding: EdgeInsets.symmetric(horizontal: 12),
                              child: Material(
                                borderRadius: BorderRadius.circular(16),
                                color: Colors.grey.shade800,
                                child: InkWell(
                                  borderRadius: BorderRadius.circular(16),
                                  onTap: () async {},
                                  child: Padding(
                                    padding: const EdgeInsets.all(10),
                                    child: Center(
                                        child: Row(
                                      mainAxisAlignment:
                                          MainAxisAlignment.center,
                                      children: [
                                        Icon(
                                          Icons.highlight_rounded,
                                          color: Colors.white,
                                          size: 16,
                                        ),
                                        SizedBox(
                                          width: 4,
                                        ),
                                        Text(
                                          'Сигнал',
                                          style: GoogleFonts.rubik(
                                              color: Colors.white,
                                              fontSize: 14),
                                        ),
                                      ],
                                    )),
                                  ),
                                ),
                              ),
                            )
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            )));
  }
}

class AppWrapper extends StatefulWidget {
  @override
  _AppWrapperState createState() => _AppWrapperState();
}

class _AppWrapperState extends State<AppWrapper> {
  static const platform = const MethodChannel('samples.flutter.dev/battery');

  final mapController = MapController();
  LatLng pointLocation = LatLng(50.443513, 30.540421);
  LatLng userLocation = LatLng(50.443513, 30.540421);
  location.Location l;
  bool loading = true;

  // final StreamController pathController = StreamController<Null>();

  var path = <LatLng>[];

  @override
  void initState() {
    _init();

    super.initState();
  }

  void _init() async {
    platform.invokeMethod('init');

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
    l = new location.Location();

    var _locationData = await l.getLocation();

    setState(() {
      pointLocation = LatLng(_locationData.latitude, _locationData.longitude);
      userLocation = pointLocation;
      loading = false;
    });

    print(pointLocation);

    mapController.move(pointLocation, 14);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          FlutterMap(
              mapController: mapController,
              options: MapOptions(
                  center: userLocation,
                  zoom: 14,
                  onTap: (LatLng val) {
                    setState(() {
                      pointLocation = val;
                    });
                  }),
              layers: [
                TileLayerOptions(
                  urlTemplate:
                      "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
                  subdomains: ['a', 'b', 'c'],
                ),
                MarkerLayerOptions(
                  // rebuild: rebuildUserLocation.stream,
                  markers: [
                    Marker(
                      width: 80.0,
                      height: 80.0,
                      point: userLocation,
                      builder: (ctx) => Container(
                        child: Icon(
                          Icons.location_on,
                          size: 30,
                          color: Colors.blueGrey.shade700,
                        ),
                      ),
                    ),
                    Marker(
                      width: 80.0,
                      height: 80.0,
                      point: pointLocation,
                      builder: (ctx) => Container(
                        child: Icon(
                          Icons.directions_car_rounded,
                          size: 32,
                          color: Colors.blueGrey.shade700,
                        ),
                      ),
                    ),
                  ],
                ),
                PolylineLayerOptions(
                    // rebuild: pathController.stream,
                    key: UniqueKey(),
                    polylines: [
                      Polyline(
                        points: path,
                        isDotted: true,
                        color: Colors.blueGrey.shade600,
                        strokeWidth: 6,
                      )
                    ])
              ]),
          if (loading)
            Positioned.fill(
              child: BackdropFilter(
                filter: ImageFilter.blur(sigmaX: 6, sigmaY: 6),
                child: Center(
                  child: CircularProgressIndicator(),
                ),
              ),
            ),
          Align(
            alignment: Alignment.bottomRight,
            child: Padding(
              padding: const EdgeInsets.only(bottom: 130, right: 18),
              child: FloatingActionButton(
                elevation: 0,
                child: Icon(Icons.gps_fixed),
                backgroundColor: Colors.blueGrey.shade400,
                onPressed: loading
                    ? null
                    : () async {
                        setState(() {
                          loading = true;
                        });

                        var _locationData = await l.getLocation();

                        setState(() {
                          pointLocation = LatLng(
                              _locationData.latitude, _locationData.longitude);
                          userLocation = pointLocation;
                          loading = false;
                        });

                        mapController.move(pointLocation, 14);
                      },
              ),
            ),
          ),
          Align(
            alignment: Alignment.bottomCenter,
            child: ClipRRect(
              borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
              child: BackdropFilter(
                filter: ImageFilter.blur(sigmaX: 6, sigmaY: 6),
                child: Container(
                    width: double.infinity,
                    height: 120,
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.2),
                    ),
                    padding: EdgeInsets.symmetric(vertical: 34, horizontal: 22),
                    child: Material(
                      borderRadius: BorderRadius.circular(16),
                      color: Colors.blueGrey.shade500,
                      child: InkWell(
                        borderRadius: BorderRadius.circular(16),
                        onTap: () async {
                          final String r =
                              await platform.invokeMethod('getDirections', {
                            'text1':
                                '${userLocation.latitude},${userLocation.longitude}',
                            'text2':
                                '${pointLocation.latitude},${pointLocation.longitude}'
                          });
                          final arr = r.split('|');
                          arr.removeAt(0);

                          final List<LatLng> newPoints = [];
                          for (final r in arr) {
                            final pa = p.Polyline.Decode(encodedString: r);

                            for (final point in pa.decodedCoords) {
                              newPoints.add(LatLng(point[0], point[1]));
                            }
                          }

                          setState(() {
                            path = newPoints;
                          });
                          // pathController.add(Null);

                          print(path);
                          print('fl');

                          SharedPreferences prefs =
                              await SharedPreferences.getInstance();

                          await prefs.setString("from",
                              '${userLocation.latitude},${userLocation.longitude}');

                         await prefs.setString("to",
                              '${pointLocation.latitude},${pointLocation.longitude}');
                          print('setting opoints');
                          print(path);
                          final points = <String>[];
                          for (final el in path) {
                            points.add('${el.latitude},${el.longitude}');
                          }
                         await prefs.setString("points", jsonEncode(points));

                          print('readingggg');

                          print(prefs.getString('points'));

                          await platform.invokeMethod('openAr', {
                            'text1':
                                '${userLocation.latitude},${userLocation.longitude}',
                            'text2':
                                '${pointLocation.latitude},${pointLocation.longitude}'
                          });
                        },
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Center(
                              child: Text(
                            'Прокласти маршрут до автомобіля',
                            style: GoogleFonts.rubik(
                                color: Colors.white, fontSize: 16),
                          )),
                        ),
                      ),
                    )),
              ),
            ),
          )
        ],
      ),
    );
  }
}
