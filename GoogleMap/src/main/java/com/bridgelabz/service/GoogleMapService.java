package com.bridgelabz.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.bridgelabz.model.LatLng;
import com.bridgelabz.model.LocationDetails;
import com.bridgelabz.util.Utility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleMapService {
	
	@Value("${googlemap.key}")
	String key;
	
	@Autowired
	Utility utility;
	
	/**
	 * @param source
	 * @param destination
	 * @return
	 */
	public Map<String,Integer> getDistance(LatLng source,LatLng destination) {
		
		String origin=source.getLat()+","+source.getLng();
		String destinations=destination.getLat()+","+destination.getLng();
	
		String mapApiUrl="https://maps.googleapis.com/maps/api/distancematrix/json?"
						  + "origins="+origin+"&destinations="+destinations
						  + "&key="+key;
		
	
		ResteasyClient restCall = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = restCall.target(mapApiUrl);

		
		Response response = target.request()
							.accept(MediaType.APPLICATION_JSON)
							.get();

		
		String responseString=response.readEntity(String.class);
		
		ObjectMapper mapper=new ObjectMapper();
		
		JsonNode responseJson=null;
		try {
			 responseJson = mapper.readTree(responseString);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int distanceInMeters=responseJson.get("rows").get(0).get("elements").get(0).get("distance").get("value").asInt();
		
		Map<String,Integer> distance=new HashMap<>();
		distance.put("distance", distanceInMeters);
		restCall.close();
		return distance;
	}
	
	/**
	 * @param currentLocation
	 * @return
	 * @throws InterruptedException 
	 */
	public List<LocationDetails> getHousingComplexes(LatLng currentLocation) throws InterruptedException {
		
		
		String location=currentLocation.getLat()+","+currentLocation.getLng();
		
		String mapApiUrl="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+location
						+"&radius=400&keyword=Apartment|CHS&strictbounds&key="+key;
		
		List<LocationDetails> housingComplexes=new ArrayList<>();
		
		ResteasyClient restCall = new ResteasyClientBuilder().build();
		
		ResteasyWebTarget target = restCall.target(mapApiUrl);

		Response response = target.request()
							.accept(MediaType.APPLICATION_JSON)
							.get();
		
		String responseString=response.readEntity(String.class);

		ObjectMapper mapper=new ObjectMapper();
		
		JsonNode responseJson=null;
		try {
			 responseJson = mapper.readTree(responseString);
			 JsonNode results=responseJson.get("results");
			 JsonNode nextPageResults=null;
			 
		if(responseJson.get("next_page_token")!=null){
			
			String nextPageToken=responseJson.get("next_page_token").asText();
			String nextPageUrl=mapApiUrl+"&pagetoken="+nextPageToken;
			System.out.println(nextPageUrl);

			target = restCall.target(nextPageUrl);
			 response = target.request()
								.accept(MediaType.APPLICATION_JSON)
								.get();
			
			String newPageResponse=response.readEntity(String.class);
			 System.out.println(newPageResponse);
			JsonNode nextPageResponse=mapper.readTree(newPageResponse);
			 nextPageResults=nextPageResponse.get("results");
			 System.out.println(nextPageResults);
		}
		
		for(int i=0;i<results.size();i++)
		{
			LocationDetails complex=new  LocationDetails();
			
			double lat=results.get(i).get("geometry").get("location").get("lat").asDouble();
			double lng=results.get(i).get("geometry").get("location").get("lng").asDouble();
			
			LatLng latlng=new LatLng(lat,lng);
			complex.setName(results.get(i).get("name").asText());
			complex.setAddress(results.get(i).get("vicinity").asText());
			complex.setLocation(latlng);
			System.out.println(i);
			housingComplexes.add(complex);
			
			if((results.size()-1)==i && nextPageResults!=null) {
				i=0;
				results=nextPageResults;
				nextPageResults=null;
			}
		}
		} catch (IOException e) {
			e.printStackTrace();
		}

		restCall.close();
		return housingComplexes;
	}
	
	
	/**
	 * @param currentLocation
	 * @return
	 */
	public List<LocationDetails> getNearByPlaces(LatLng currentLocation) {
		
		String location=currentLocation.getLat()+","+currentLocation.getLng();
		
		List<LocationDetails> nearByPlaces=new ArrayList<>();
		
		ObjectMapper mapper=new ObjectMapper();
		
		ResteasyClient restCall = new ResteasyClientBuilder().build();
		
		for(int radius=500;radius<=4000;radius+=1000) {
			
		String mapApiUrl="https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
				+ "location="+location+"&radius="+radius+"&types=sublocality&key="+key;
		
		
		ResteasyWebTarget target = restCall.target(mapApiUrl);

		Response response = target.request()
							.accept(MediaType.APPLICATION_JSON)
							.get();

		String responseString=response.readEntity(String.class);
	
		JsonNode responseJson=null;
		
		try {
			 responseJson = mapper.readTree(responseString);
 		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		JsonNode results=responseJson.get("results");
		
		for(int i=0;i<results.size();i++)
		{
			int flag=0;
			
			double lat=results.get(i).get("geometry").get("location").get("lat").asDouble();
			double lng=results.get(i).get("geometry").get("location").get("lng").asDouble();
			
			LatLng latlng=new LatLng(lat,lng);
			LocationDetails place = new  LocationDetails();
			place .setName(results.get(i).get("name").asText());
			place .setAddress(results.get(i).get("vicinity").asText());
			place .setLocation(latlng);
			
			for(int j=0;j<nearByPlaces.size();j++) {
				if(nearByPlaces.get(j).getName().equals(place .getName())) {
					flag=1;
				}
			}
			
			if(flag==0) {
				nearByPlaces.add(place );
			}
		}
		
		}
		restCall.close();
		return nearByPlaces;
	}
}
