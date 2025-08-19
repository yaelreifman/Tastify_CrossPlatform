//
//  MapView.swift
//  iosApp
//
//  Created by sharon bronshteyn on 19/08/2025.
//

import SwiftUI
import CoreLocation
import GoogleMaps

struct GoogleMapView: UIViewRepresentable {
    let coordinate: CLLocationCoordinate2D
    var title: String? = nil
    var zoom: Float = 15

    func makeUIView(context: Context) -> GMSMapView {
        let camera = GMSCameraPosition.camera(withLatitude: coordinate.latitude,
                                              longitude: coordinate.longitude,
                                              zoom: zoom)
        let mapView = GMSMapView(frame: .zero, camera: camera)

        // מרקר
        let marker = GMSMarker(position: coordinate)
        marker.title = title
        marker.map = mapView

        return mapView
    }

    func updateUIView(_ mapView: GMSMapView, context: Context) {
        // אפשר לעדכן כאן אם הקואורדינטות משתנות
    }
}
