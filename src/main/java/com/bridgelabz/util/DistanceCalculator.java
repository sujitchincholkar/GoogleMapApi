package com.bridgelabz.util;



public class DistanceCalculator {


	
	public double distance(double lat1, double lon1, double lat2, double lon2, String sr) {
	
	
	    double theta = lon1 - lon2;
	    double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + 
	    		Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
	    
	    dist = Math.acos(dist);
	    dist = rad2deg(dist);
	    dist = dist * 60 * 1.1515;
	    if (sr.equals("K")) {
	      dist = dist * 1.609344;
	    } else if (sr.equals("N")) {
	    	dist = dist * 0.8684;
	      }
	    return (dist);
	  }
	public double deg2rad(double deg) {
	    return (deg * Math.PI / 180.0);
	  }
	public double rad2deg(double rad) {
	    return (rad * 180.0 / Math.PI);
	  }
}
