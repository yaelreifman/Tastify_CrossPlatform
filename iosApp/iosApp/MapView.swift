// GoogleMapView.swift
import SwiftUI
import GoogleMaps
import CoreLocation

struct GoogleMapView: UIViewRepresentable {
    var coordinate: CLLocationCoordinate2D
    var zoom: Float = 15
    var title: String? = nil

    func makeCoordinator() -> Coordinator { Coordinator() }

    func makeUIView(context: Context) -> GMSMapView {
        let camera = GMSCameraPosition.camera(withLatitude: coordinate.latitude,
                                              longitude: coordinate.longitude,
                                              zoom: zoom)
        let map = GMSMapView(frame: .zero, camera: camera)
        map.settings.compassButton = true
        context.coordinator.marker.position = coordinate
        context.coordinator.marker.title = title
        context.coordinator.marker.map = map
        return map
    }

    func updateUIView(_ map: GMSMapView, context: Context) {
        let camera = GMSCameraPosition.camera(withLatitude: coordinate.latitude,
                                              longitude: coordinate.longitude,
                                              zoom: zoom)
        map.animate(to: camera)
        context.coordinator.marker.position = coordinate
        context.coordinator.marker.title = title
        context.coordinator.marker.map = map
    }

    final class Coordinator {
        let marker = GMSMarker()
    }
}
