package com.bridgelabz.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.bridgelabz.model.LatLng;
import com.bridgelabz.model.LocationDetails;
import com.bridgelabz.util.ResponseJsonUtil;
import com.bridgelabz.util.RestCallUtility;
import com.fasterxml.jackson.databind.JsonNode;

public class MapServiceImpl implements MapService {

	@Value("${googlemap.key}")
	private String key;

	@Autowired
	RestCallUtility restCallUtil;

	/**
	 * @param source
	 * @param destination
	 * @return
	 */
	public Map<String, Integer> getDistance(LatLng source, LatLng destination) {

		Map<String, Integer> distance = new HashMap<>();
		try {
			String origin = source.getLat() + "," + source.getLng();
			String destinations = destination.getLat() + "," + destination.getLng();

			String mapApiUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?" + "origins=" + origin
					+ "&destinations=" + destinations + "&key=" + key;

			JsonNode responseJson = restCallUtil.getResponse(mapApiUrl);

			int distanceInMeters = responseJson.get("rows").get(0).get("elements").get(0).get("distance").get("value")
					.asInt();

			distance.put("distance", (int) distanceInMeters);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return distance;
	}

	/**
	 * @param currentLocation
	 * @return
	 */
	public List<LocationDetails> getHousingComplexes(LatLng currentLocation) {

		String location = currentLocation.getLat() + "," + currentLocation.getLng();

		String mapApiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location
				+ "&radius=900&keyword=Apartment|Society|CHS|Complex&strictbounds&key=" + key;

		List<LocationDetails> housingComplexes = new ArrayList<>();

		ResponseJsonUtil responseUtil = new ResponseJsonUtil();
		int counter = 0;
		try {

			JsonNode responseJson = restCallUtil.getResponse(mapApiUrl);

			JsonNode results = responseJson.get("results");
			housingComplexes.addAll(responseUtil.getHousingComplex(results));

			JsonNode nextPageResults = null;
			if (responseJson.get("next_page_token") != null) {
				String nextPageToken = responseJson.get("next_page_token").asText();

				while (nextPageToken != null) {
					counter++;
					String nextPageUrl = mapApiUrl + "&pagetoken=" + nextPageToken;
					// two seconds delay required for consecutive requests
					Thread.sleep(2000);
					JsonNode nextPageResponse = restCallUtil.getResponse(nextPageUrl);
					nextPageResults = nextPageResponse.get("results");
					housingComplexes.addAll(responseUtil.getHousingComplex(nextPageResults));

					if (nextPageResponse.get("next_page_token") != null) {
						nextPageToken = nextPageResponse.get("next_page_token").asText();
					} else {
						nextPageToken = null;
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(housingComplexes.size() + " " + counter);
		return housingComplexes;
	}

	/**
	 * @param currentLocation
	 * @return
	 */
	public List<LocationDetails> getNearByPlaces(LatLng currentLocation) {

		String location = currentLocation.getLat() + "," + currentLocation.getLng();

		List<LocationDetails> nearByPlaces = new ArrayList<>();

		for (int radius = 500; radius <= 4000; radius += 1000) {

			String mapApiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location=" + location
					+ "&radius=" + radius + "&types=sublocality_level_1&key=" + key;

			JsonNode responseJson = null;
			try {
				responseJson = restCallUtil.getResponse(mapApiUrl);
			} catch (IOException e) {
				e.printStackTrace();
			}

			JsonNode results = responseJson.get("results");

			for (int i = 0; i < results.size(); i++) {
				int flag = 0;

				double lat = results.get(i).get("geometry").get("location").get("lat").asDouble();
				double lng = results.get(i).get("geometry").get("location").get("lng").asDouble();
				LatLng latlng = new LatLng(lat, lng);

				LocationDetails place = new LocationDetails();
				place.setName(results.get(i).get("name").asText());
				place.setAddress(results.get(i).get("vicinity").asText());
				place.setLocation(latlng);

				for (int j = 0; j < nearByPlaces.size(); j++) {
					if (nearByPlaces.get(j).getName().equals(place.getName())) {
						flag = 1;
					}
				}
				if (flag == 0) {
					nearByPlaces.add(place);
				}
			}

		}

		return nearByPlaces;
	}

	/**
	 * @param searchString
	 * @return
	 */
	public Map<String, Object> getPlaceInfo(String searchString) {

		Map<String, Object> placeInfo = new HashMap<>();

		String[] words = searchString.split(" ");
		searchString = "";

		int i = 0;
		while (i < words.length) {
			searchString = words[i] + "+" + searchString;
			i++;
		}
		String mapApiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + searchString + "&key=" + key;

		JsonNode responseJson = null;

		try {
			responseJson = restCallUtil.getResponse(mapApiUrl);

			JsonNode results = responseJson.get("results");

			double lat = results.get(0).get("geometry").get("location").get("lat").asDouble();
			double lng = results.get(0).get("geometry").get("location").get("lng").asDouble();

			String location = lat + "," + lng;
			String geoCodeUrl = "https://maps.googleapis.com/maps/api/geocode/json?&types=postal_code&latlng="
					+ location + "&keyword=" + words[0] + "&rankBy=keyword&key=" + key;

			responseJson = restCallUtil.getResponse(geoCodeUrl);

			int flag = 0;
			for (int k = 0; k < responseJson.get("results").size(); k++) {

				results = responseJson.get("results").get(k);
				for (int index = 0; index < results.get("types").size(); index++) {
					if (results.get("types").get(index).asText().equals("postal_code")) {
						flag = 1;
						break;
					}
				}
				if (flag == 1) {
					break;
				}
			}
			JsonNode record = results.get("address_components");
			for (int j = 0; j < record.size(); j++) {

				for (int index = 0; index < record.get(j).get("types").size(); index++) {
					String type = record.get(j).get("types").get(index).asText();

					if (type.equals("postal_code")) {
						placeInfo.put("zipcode", record.get(j).get("long_name").asText());

					} else if (type.equals("sublocality_level_1")) {
						placeInfo.put("sublocality_level_1", record.get(j).get("long_name").asText());

					} else if (type.equals("locality")) {
						placeInfo.put("locality", record.get(j).get("long_name").asText());

					} else if (type.equals("country")) {
						placeInfo.put("country", record.get(j).get("long_name").asText());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return placeInfo;
	}

}
