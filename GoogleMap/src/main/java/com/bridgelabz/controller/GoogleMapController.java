package com.bridgelabz.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import com.bridgelabz.model.LatLng;
import com.bridgelabz.model.LocationDetails;
import com.bridgelabz.service.GoogleMapService;

@RestController
public class GoogleMapController {
	
	@Autowired
	GoogleMapService service;
	
	@RequestMapping(value="/getdistance",method=RequestMethod.POST)
	public  Map<String, Integer> getDistanceBetween(@RequestBody Map<String ,LatLng> locations) {
		LatLng source=new LatLng((double)locations.get("source").getLat(),locations.get("source").getLng());
		LatLng destination=new LatLng((double)locations.get("destination").getLat(),locations.get("destination").getLng());

		return service.getDistance(source, destination);
	}
	
	@RequestMapping(value="/gethousingcomplexes",method=RequestMethod.POST)
	public  List<LocationDetails> getHousingComplexes(@RequestBody LatLng location ) {
		
		return service.getHousingComplexes(location);
	}
	
	@RequestMapping(value="/getnearbyplaces",method=RequestMethod.POST)
	public  List<LocationDetails> getNearByPlaces(@RequestBody LatLng location ) {
		
		return service.getNearByPlaces(location);
	}

}
