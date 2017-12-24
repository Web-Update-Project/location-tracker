package com.google.location.track.api.util;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.location.track.api.common.Constants;
import com.google.location.track.api.dto.DurationTimeWrapper;
import com.google.location.track.api.dto.Location;
import com.google.location.track.api.search.dto.NearestSearchResponse;
import com.google.location.track.api.search.dto.SearchResponse;
import com.jayway.jsonpath.JsonPath;

@Component
@PropertySource(value = { "classpath:googleMap.properties" })
public class RuleUtil {

	// private Logger log = LoggerFactory.getLogger(RuleUtil.class);
	@Autowired
	private Environment environment;
	private RestTemplate template;
	ObjectMapper mapper = null;
	HttpHeaders headers = null;

	private static String API_KEY = "";
	private static String URL = "";

	@PostConstruct
	public void init() {
		template = new RestTemplate();
		mapper = new ObjectMapper();
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		template.getMessageConverters().add(
				new MappingJackson2HttpMessageConverter());
		template.getMessageConverters().add(new StringHttpMessageConverter());
	}

	public String getAddressDetails(double lat, double lan)
			throws JsonParseException, JsonMappingException, IOException {
		String jsonExpression = "$.results[0].formatted_address";
		API_KEY = environment.getProperty(Constants.GET_ADDRESS_API_KEY);
		URL = environment.getProperty(Constants.GET_ADDRESS_URL) + lat
				+ Constants.COMMA_SEPARATOR + lan + Constants.KEY_CONSTANT
				+ API_KEY;
		String result = template.getForObject(URL, String.class);
		String formattedAddress = JsonPath.parse(result).read(jsonExpression,
				String.class);
		return formattedAddress;
	}

	public DurationTimeWrapper getDuration(String source, String destination)
			throws JsonParseException, JsonMappingException, IOException {
		String distance_JsonExpression = "$.routes[0].legs[0].distance.text";
		String duration_JsonExpression = "$.routes[0].legs[0].duration.text";
		API_KEY = environment.getProperty(Constants.GET_DURATION_API_KEY);
		URL = environment.getProperty(Constants.GET_DURATION_URL) + source
				+ Constants.COMMA_SEPARATOR + Constants.DESTINATION_CONSTANT
				+ destination + Constants.KEY_CONSTANT + API_KEY;
		String result = template.getForObject(URL, String.class);
		String distance = JsonPath.parse(result).read(distance_JsonExpression,
				String.class);
		String duration = JsonPath.parse(result).read(duration_JsonExpression,
				String.class);
		return new DurationTimeWrapper(distance, duration);

	}

	public Location getLatitudeLongitude(String address)
			throws JsonParseException, JsonMappingException, IOException {
		String jsonExpression = "$.results[0].geometry.location";
		API_KEY = environment.getProperty(Constants.GET_LATLANG_API_KEY);
		URL = environment.getProperty(Constants.GET_LATLANG_URL) + address
				+ Constants.KEY_CONSTANT + API_KEY;
		String result = template.getForObject(URL, String.class);
		Location location = JsonPath.parse(result).read(jsonExpression,
				Location.class);
		return location;
	}

	public List<NearestSearchResponse> getNearestPlace(String searchType,
			String location) throws JsonParseException, JsonMappingException,
			IOException {
		API_KEY = environment.getProperty(Constants.GET_NEAREST_PLACE_API_KEY);
		URL = environment.getProperty(Constants.GET_NEAREST_PLACE_URL)
				+ searchType + "+in+" + location + Constants.KEY_CONSTANT
				+ API_KEY;
		String result = template.getForObject(URL, String.class);

		/*
		 * String jsonExpression = "$.results[*]"; List<Result> results =
		 * JsonPath.parse(result).read(jsonExpression, List.class);
		 */

		SearchResponse response = mapper
				.readValue(result, SearchResponse.class);
		List<NearestSearchResponse> resp = ServiceUtils.getSearchInfo(response,
				searchType);
		return resp;
	}

}
