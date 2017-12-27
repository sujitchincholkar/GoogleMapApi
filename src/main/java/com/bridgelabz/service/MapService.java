package com.bridgelabz.service;

import java.util.List;
import java.util.Map;

import com.bridgelabz.model.LatLng;
import com.bridgelabz.model.LocationDetails;

public interface MapService {
	public Map<String, Integer> getDistance(LatLng source, LatLng destination);
	public List<LocationDetails> getHousingComplexes(LatLng currentLocation);
	public List<LocationDetails> getNearByPlaces(LatLng currentLocation);
	public Map<String, Object> getPlaceInfo(String searchString);
}
