/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.elasticsearch.core.convert;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.skyscreamer.jsonassert.JSONAssert.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.lang.Nullable;

/**
 * Unit tests for {@link MappingElasticsearchConverter}.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @author Peter-Josef Meisch
 * @author Konrad Kurdej
 * @author Roman Puchkovskiy
 */
public class MappingElasticsearchConverterUnitTests {

	static final String JSON_STRING = "{\"_class\":\"org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverterUnitTests$Car\",\"name\":\"Grat\",\"model\":\"Ford\"}";
	static final String CAR_MODEL = "Ford";
	static final String CAR_NAME = "Grat";
	MappingElasticsearchConverter mappingElasticsearchConverter;

	Person sarahConnor;
	Person kyleReese;
	Person t800;

	Inventory gun = new Gun("Glock 19", 33);
	Inventory grenade = new Grenade("40 mm");
	Inventory rifle = new Rifle("AR-18 Assault Rifle", 3.17, 40);
	Inventory shotGun = new ShotGun("Ithaca 37 Pump Shotgun");

	Address observatoryRoad;
	Place bigBunsCafe;

	Document sarahAsMap;
	Document t800AsMap;
	Document kyleAsMap;
	Document gratiotAveAsMap;
	Document locationAsMap;
	Document gunAsMap;
	Document grenadeAsMap;
	Document rifleAsMap;
	Document shotGunAsMap;
	Document bigBunsCafeAsMap;
	Document notificationAsMap;

	@BeforeEach
	public void init() {

		SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
		mappingContext.setInitialEntitySet(Collections.singleton(Rifle.class));
		mappingContext.afterPropertiesSet();

		mappingElasticsearchConverter = new MappingElasticsearchConverter(mappingContext, new GenericConversionService());
		mappingElasticsearchConverter.setConversions(
				new ElasticsearchCustomConversions(Arrays.asList(new ShotGunToMapConverter(), new MapToShotGunConverter())));
		mappingElasticsearchConverter.afterPropertiesSet();

		sarahConnor = new Person();
		sarahConnor.id = "sarah";
		sarahConnor.name = "Sarah Connor";
		sarahConnor.gender = Gender.MAN;

		kyleReese = new Person();
		kyleReese.id = "kyle";
		kyleReese.gender = Gender.MAN;
		kyleReese.name = "Kyle Reese";

		t800 = new Person();
		t800.id = "t800";
		t800.name = "T-800";
		t800.gender = Gender.MACHINE;

		t800AsMap = Document.create();
		t800AsMap.put("id", "t800");
		t800AsMap.put("name", "T-800");
		t800AsMap.put("gender", "MACHINE");
		t800AsMap.put("_class",
				"org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverterUnitTests$Person");

		observatoryRoad = new Address();
		observatoryRoad.city = "Los Angeles";
		observatoryRoad.street = "2800 East Observatory Road";
		observatoryRoad.location = new Point(34.118347D, -118.3026284D);

		bigBunsCafe = new Place();
		bigBunsCafe.name = "Big Buns Cafe";
		bigBunsCafe.city = "Los Angeles";
		bigBunsCafe.street = "15 South Fremont Avenue";
		bigBunsCafe.location = new Point(34.0945637D, -118.1545845D);

		sarahAsMap = Document.create();
		sarahAsMap.put("id", "sarah");
		sarahAsMap.put("name", "Sarah Connor");
		sarahAsMap.put("gender", "MAN");
		sarahAsMap.put("_class",
				"org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverterUnitTests$Person");

		kyleAsMap = Document.create();
		kyleAsMap.put("id", "kyle");
		kyleAsMap.put("gender", "MAN");
		kyleAsMap.put("name", "Kyle Reese");

		locationAsMap = Document.create();
		locationAsMap.put("lat", 34.118347D);
		locationAsMap.put("lon", -118.3026284D);

		gratiotAveAsMap = Document.create();
		gratiotAveAsMap.put("city", "Los Angeles");
		gratiotAveAsMap.put("street", "2800 East Observatory Road");
		gratiotAveAsMap.put("location", locationAsMap);

		bigBunsCafeAsMap = Document.create();
		bigBunsCafeAsMap.put("name", "Big Buns Cafe");
		bigBunsCafeAsMap.put("city", "Los Angeles");
		bigBunsCafeAsMap.put("street", "15 South Fremont Avenue");
		bigBunsCafeAsMap.put("location", new LinkedHashMap<>());
		((HashMap<String, Object>) bigBunsCafeAsMap.get("location")).put("lat", 34.0945637D);
		((HashMap<String, Object>) bigBunsCafeAsMap.get("location")).put("lon", -118.1545845D);
		bigBunsCafeAsMap.put("_class",
				"org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverterUnitTests$Place");

		gunAsMap = Document.create();
		gunAsMap.put("label", "Glock 19");
		gunAsMap.put("shotsPerMagazine", 33);
		gunAsMap.put("_class", Gun.class.getName());

		grenadeAsMap = Document.create();
		grenadeAsMap.put("label", "40 mm");
		grenadeAsMap.put("_class", Grenade.class.getName());

		rifleAsMap = Document.create();
		rifleAsMap.put("label", "AR-18 Assault Rifle");
		rifleAsMap.put("weight", 3.17D);
		rifleAsMap.put("maxShotsPerMagazine", 40);
		rifleAsMap.put("_class", "rifle");

		shotGunAsMap = Document.create();
		shotGunAsMap.put("model", "Ithaca 37 Pump Shotgun");
		shotGunAsMap.put("_class", ShotGun.class.getName());

		notificationAsMap = Document.create();
		notificationAsMap.put("id", 1L);
		notificationAsMap.put("fromEmail", "from@email.com");
		notificationAsMap.put("toEmail", "to@email.com");
		Map<String, Object> data = new HashMap<>();
		data.put("documentType", "abc");
		data.put("content", null);
		notificationAsMap.put("params", data);
		notificationAsMap.put("_class",
				"org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverterUnitTests$Notification");
	}

	@Test
	public void shouldFailToInitializeGivenMappingContextIsNull() {

		// given
		assertThatThrownBy(() -> new MappingElasticsearchConverter(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void shouldReturnMappingContextWithWhichItWasInitialized() {

		// given
		MappingContext mappingContext = new SimpleElasticsearchMappingContext();
		MappingElasticsearchConverter converter = new MappingElasticsearchConverter(mappingContext);

		// then
		assertThat(converter.getMappingContext()).isNotNull();
		assertThat(converter.getMappingContext()).isSameAs(mappingContext);
	}

	@Test
	public void shouldReturnDefaultConversionService() {

		// given
		MappingElasticsearchConverter converter = new MappingElasticsearchConverter(
				new SimpleElasticsearchMappingContext());

		// when
		ConversionService conversionService = converter.getConversionService();

		// then
		assertThat(conversionService).isNotNull();
	}

	@Test // DATAES-530
	public void shouldMapObjectToJsonString() {
		// Given

		// When
		String jsonResult = mappingElasticsearchConverter.mapObject(Car.builder().model(CAR_MODEL).name(CAR_NAME).build())
				.toJson();

		// Then
		assertThat(jsonResult).isEqualTo(JSON_STRING);
	}

	@Test // DATAES-530
	public void shouldReadJsonStringToObject() {
		// Given

		// When
		Car result = mappingElasticsearchConverter.read(Car.class, Document.parse(JSON_STRING));

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(CAR_NAME);
		assertThat(result.getModel()).isEqualTo(CAR_MODEL);
	}

	@Test // DATAES-530
	public void shouldMapGeoPointElasticsearchNames() {
		// given
		Point point = new Point(10, 20);
		String pointAsString = point.getX() + "," + point.getY();
		double[] pointAsArray = { point.getX(), point.getY() };
		GeoEntity geoEntity = GeoEntity.builder().pointA(point).pointB(GeoPoint.fromPoint(point)).pointC(pointAsString)
				.pointD(pointAsArray).build();
		// when
		String jsonResult = mappingElasticsearchConverter.mapObject(geoEntity).toJson();

		// then
		assertThat(jsonResult).contains(pointTemplate("pointA", point));
		assertThat(jsonResult).contains(pointTemplate("pointB", point));
		assertThat(jsonResult).contains(String.format(Locale.ENGLISH, "\"%s\":\"%s\"", "pointC", pointAsString));
		assertThat(jsonResult)
				.contains(String.format(Locale.ENGLISH, "\"%s\":[%.1f,%.1f]", "pointD", pointAsArray[0], pointAsArray[1]));
	}

	@Test // DATAES-530
	public void ignoresReadOnlyProperties() {

		// given
		Sample sample = new Sample();
		sample.readOnly = "readOnly";
		sample.property = "property";
		sample.javaTransientProperty = "javaTransient";
		sample.annotatedTransientProperty = "transient";

		// when
		String result = mappingElasticsearchConverter.mapObject(sample).toJson();

		// then
		assertThat(result).contains("\"property\"");
		assertThat(result).contains("\"javaTransient\"");

		assertThat(result).doesNotContain("readOnly");
		assertThat(result).doesNotContain("annotatedTransientProperty");
	}

	@Test // DATAES-530
	public void writesNestedEntity() {

		Person person = new Person();
		person.birthDate = LocalDate.now();
		person.gender = Gender.MAN;
		person.address = observatoryRoad;

		Map<String, Object> sink = writeToMap(person);

		assertThat(sink.get("address")).isEqualTo(gratiotAveAsMap);
	}

	@Test // DATAES-530
	public void writesConcreteList() {

		Person ginger = new Person();
		ginger.id = "ginger";
		ginger.gender = Gender.MAN;

		sarahConnor.coWorkers = Arrays.asList(kyleReese, ginger);

		Map<String, Object> target = writeToMap(sarahConnor);
		assertThat((List) target.get("coWorkers")).hasSize(2).contains(kyleAsMap);
	}

	@Test // DATAES-530
	public void writesInterfaceList() {

		Inventory gun = new Gun("Glock 19", 33);
		Inventory grenade = new Grenade("40 mm");

		sarahConnor.inventoryList = Arrays.asList(gun, grenade);

		Map<String, Object> target = writeToMap(sarahConnor);
		assertThat((List) target.get("inventoryList")).containsExactly(gunAsMap, grenadeAsMap);
	}

	@Test // DATAES-530
	public void readTypeCorrectly() {

		Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);

		assertThat(target).isEqualTo(sarahConnor);
	}

	@Test // DATAES-530
	public void readListOfConcreteTypesCorrectly() {

		sarahAsMap.put("coWorkers", Collections.singletonList(kyleAsMap));

		Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);

		assertThat(target.getCoWorkers()).contains(kyleReese);
	}

	@Test // DATAES-530
	public void readListOfInterfacesTypesCorrectly() {

		sarahAsMap.put("inventoryList", Arrays.asList(gunAsMap, grenadeAsMap));

		Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);

		assertThat(target.getInventoryList()).containsExactly(gun, grenade);
	}

	@Test // DATAES-530
	public void writeMapOfConcreteType() {

		sarahConnor.shippingAddresses = new LinkedHashMap<>();
		sarahConnor.shippingAddresses.put("home", observatoryRoad);

		Map<String, Object> target = writeToMap(sarahConnor);
		assertThat(target.get("shippingAddresses")).isInstanceOf(Map.class);
		assertThat(target.get("shippingAddresses")).isEqualTo(Collections.singletonMap("home", gratiotAveAsMap));
	}

	@Test // DATAES-530
	public void writeMapOfInterfaceType() {

		sarahConnor.inventoryMap = new LinkedHashMap<>();
		sarahConnor.inventoryMap.put("glock19", gun);
		sarahConnor.inventoryMap.put("40 mm grenade", grenade);

		Map<String, Object> target = writeToMap(sarahConnor);
		assertThat(target.get("inventoryMap")).isInstanceOf(Map.class);
		assertThat((Map) target.get("inventoryMap")).containsEntry("glock19", gunAsMap).containsEntry("40 mm grenade",
				grenadeAsMap);
	}

	@Test // DATAES-530
	public void readConcreteMapCorrectly() {

		sarahAsMap.put("shippingAddresses", Collections.singletonMap("home", gratiotAveAsMap));

		Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);

		assertThat(target.getShippingAddresses()).hasSize(1).containsEntry("home", observatoryRoad);
	}

	@Test // DATAES-530
	public void readInterfaceMapCorrectly() {

		sarahAsMap.put("inventoryMap", Collections.singletonMap("glock19", gunAsMap));

		Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);

		assertThat(target.getInventoryMap()).hasSize(1).containsEntry("glock19", gun);
	}

	@Test // DATAES-530
	public void genericWriteList() {

		Skynet skynet = new Skynet();
		skynet.objectList = new ArrayList<>();
		skynet.objectList.add(t800);
		skynet.objectList.add(gun);

		Map<String, Object> target = writeToMap(skynet);

		assertThat((List<Object>) target.get("objectList")).containsExactly(t800AsMap, gunAsMap);
	}

	@Test // DATAES-530
	public void readGenericList() {

		Document source = Document.create();
		source.put("objectList", Arrays.asList(t800AsMap, gunAsMap));

		Skynet target = mappingElasticsearchConverter.read(Skynet.class, source);

		assertThat(target.getObjectList()).containsExactly(t800, gun);
	}

	@Test // DATAES-530
	public void genericWriteListWithList() {

		Skynet skynet = new Skynet();
		skynet.objectList = new ArrayList<>();
		skynet.objectList.add(Arrays.asList(t800, gun));

		Map<String, Object> target = writeToMap(skynet);

		assertThat((List<Object>) target.get("objectList")).containsExactly(Arrays.asList(t800AsMap, gunAsMap));
	}

	@Test // DATAES-530
	public void readGenericListList() {

		Document source = Document.create();
		source.put("objectList", Collections.singletonList(Arrays.asList(t800AsMap, gunAsMap)));

		Skynet target = mappingElasticsearchConverter.read(Skynet.class, source);

		assertThat(target.getObjectList()).containsExactly(Arrays.asList(t800, gun));
	}

	@Test // DATAES-530
	public void writeGenericMap() {

		Skynet skynet = new Skynet();
		skynet.objectMap = new LinkedHashMap<>();
		skynet.objectMap.put("gun", gun);
		skynet.objectMap.put("grenade", grenade);

		Map<String, Object> target = writeToMap(skynet);

		assertThat((Map<String, Object>) target.get("objectMap")).containsEntry("gun", gunAsMap).containsEntry("grenade",
				grenadeAsMap);
	}

	@Test // DATAES-530
	public void readGenericMap() {

		Document source = Document.create();
		source.put("objectMap", Collections.singletonMap("glock19", gunAsMap));

		Skynet target = mappingElasticsearchConverter.read(Skynet.class, source);

		assertThat(target.getObjectMap()).containsEntry("glock19", gun);
	}

	@Test // DATAES-530
	public void writeGenericMapMap() {

		Skynet skynet = new Skynet();
		skynet.objectMap = new LinkedHashMap<>();
		skynet.objectMap.put("inventory", Collections.singletonMap("glock19", gun));

		Map<String, Object> target = writeToMap(skynet);

		assertThat((Map<String, Object>) target.get("objectMap")).containsEntry("inventory",
				Collections.singletonMap("glock19", gunAsMap));
	}

	@Test // DATAES-530
	public void readGenericMapMap() {

		Document source = Document.create();
		source.put("objectMap", Collections.singletonMap("inventory", Collections.singletonMap("glock19", gunAsMap)));

		Skynet target = mappingElasticsearchConverter.read(Skynet.class, source);

		assertThat(target.getObjectMap()).containsEntry("inventory", Collections.singletonMap("glock19", gun));
	}

	@Test // DATAES-530
	public void readsNestedEntity() {

		sarahAsMap.put("address", gratiotAveAsMap);

		Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);

		assertThat(target.getAddress()).isEqualTo(observatoryRoad);
	}

	@Test // DATAES-530
	public void readsNestedObjectEntity() {

		Document source = Document.create();
		source.put("object", t800AsMap);

		Skynet target = mappingElasticsearchConverter.read(Skynet.class, source);

		assertThat(target.getObject()).isEqualTo(t800);
	}

	@Test // DATAES-530
	public void writesAliased() {
		assertThat(writeToMap(rifle)).containsEntry("_class", "rifle").doesNotContainValue(Rifle.class.getName());
	}

	@Test // DATAES-530
	public void writesNestedAliased() {

		t800.inventoryList = Collections.singletonList(rifle);
		Map<String, Object> target = writeToMap(t800);

		assertThat((List) target.get("inventoryList")).contains(rifleAsMap);
	}

	@Test // DATAES-530
	public void readsAliased() {
		assertThat(mappingElasticsearchConverter.read(Inventory.class, rifleAsMap)).isEqualTo(rifle);
	}

	@Test // DATAES-530
	public void readsNestedAliased() {

		t800AsMap.put("inventoryList", Collections.singletonList(rifleAsMap));

		assertThat(mappingElasticsearchConverter.read(Person.class, t800AsMap).getInventoryList()).containsExactly(rifle);
	}

	@Test // DATAES-530
	public void appliesCustomConverterForWrite() {
		assertThat(writeToMap(shotGun)).isEqualTo(shotGunAsMap);
	}

	@Test // DATAES-530
	public void appliesCustomConverterForRead() {
		assertThat(mappingElasticsearchConverter.read(Inventory.class, shotGunAsMap)).isEqualTo(shotGun);
	}

	@Test // DATAES-530
	public void writeSubTypeCorrectly() {

		sarahConnor.address = bigBunsCafe;

		Map<String, Object> target = writeToMap(sarahConnor);

		assertThat(target.get("address")).isEqualTo(bigBunsCafeAsMap);
	}

	@Test // DATAES-530
	public void readSubTypeCorrectly() {

		sarahAsMap.put("address", bigBunsCafeAsMap);

		Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);

		assertThat(target.address).isEqualTo(bigBunsCafe);
	}

	@Test // DATAES-716
	void shouldWriteLocalDate() throws JSONException {
		Person person = new Person();
		person.id = "4711";
		person.firstName = "John";
		person.lastName = "Doe";
		person.birthDate = LocalDate.of(2000, 8, 22);
		person.gender = Gender.MAN;

		String expected = '{' + //
				"  \"id\": \"4711\"," + //
				"  \"first-name\": \"John\"," + //
				"  \"last-name\": \"Doe\"," + //
				"  \"birth-date\": \"22.08.2000\"," + //
				"  \"gender\": \"MAN\"" + //
				'}';
		Document document = Document.create();
		mappingElasticsearchConverter.write(person, document);
		String json = document.toJson();

		assertEquals(expected, json, false);
	}

	@Test
	void shouldReadLocalDate() {
		Document document = Document.create();
		document.put("id", "4711");
		document.put("first-name", "John");
		document.put("last-name", "Doe");
		document.put("birth-date", "22.08.2000");
		document.put("gender", "MAN");

		Person person = mappingElasticsearchConverter.read(Person.class, document);

		assertThat(person.getId()).isEqualTo("4711");
		assertThat(person.getBirthDate()).isEqualTo(LocalDate.of(2000, 8, 22));
		assertThat(person.getGender()).isEqualTo(Gender.MAN);
	}

	@Test // DATAES-763
	void writeEntityWithMapDataType() {

		Notification notification = new Notification();
		notification.fromEmail = "from@email.com";
		notification.toEmail = "to@email.com";
		Map<String, Object> data = new HashMap<>();
		data.put("documentType", "abc");
		data.put("content", null);
		notification.params = data;
		notification.id = 1L;

		Document document = Document.create();
		mappingElasticsearchConverter.write(notification, document);
		assertThat(document).isEqualTo(notificationAsMap);
	}

	@Test // DATAES-763
	void readEntityWithMapDataType() {

		Document document = Document.create();
		document.put("id", 1L);
		document.put("fromEmail", "from@email.com");
		document.put("toEmail", "to@email.com");
		Map<String, Object> data = new HashMap<>();
		data.put("documentType", "abc");
		data.put("content", null);
		document.put("params", data);

		Notification notification = mappingElasticsearchConverter.read(Notification.class, document);
		assertThat(notification.params.get("documentType")).isEqualTo("abc");
		assertThat(notification.params.get("content")).isNull();
	}

	@Test // DATAES-795
	void readGenericMapWithSimpleTypes() {
		Map<String, Object> mapWithSimpleValues = new HashMap<>();
		mapWithSimpleValues.put("int", 1);
		mapWithSimpleValues.put("string", "string");
		mapWithSimpleValues.put("boolean", true);

		Document document = Document.create();
		document.put("schemaLessObject", mapWithSimpleValues);

		SchemaLessObjectWrapper wrapper = mappingElasticsearchConverter.read(SchemaLessObjectWrapper.class, document);
		assertThat(wrapper.getSchemaLessObject()).isEqualTo(mapWithSimpleValues);
	}

	@Test // DATAES-797
	void readGenericListWithMaps() {
		Map<String, Object> simpleMap = new HashMap<>();
		simpleMap.put("int", 1);

		List<Map<String, Object>> listWithSimpleMap = new ArrayList<>();
		listWithSimpleMap.add(simpleMap);

		Map<String, List<Map<String, Object>>> mapWithSimpleList = new HashMap<>();
		mapWithSimpleList.put("someKey", listWithSimpleMap);

		Document document = Document.create();
		document.put("schemaLessObject", mapWithSimpleList);

		SchemaLessObjectWrapper wrapper = mappingElasticsearchConverter.read(SchemaLessObjectWrapper.class, document);
		assertThat(wrapper.getSchemaLessObject()).isEqualTo(mapWithSimpleList);
	}

	@Test // DATAES-799
	void shouldNotWriteSeqNoPrimaryTermProperty() {
		EntityWithSeqNoPrimaryTerm entity = new EntityWithSeqNoPrimaryTerm();
		entity.seqNoPrimaryTerm = new SeqNoPrimaryTerm(1L, 2L);
		Document document = Document.create();

		mappingElasticsearchConverter.write(entity, document);

		assertThat(document).doesNotContainKey("seqNoPrimaryTerm");
	}

	@Test // DATAES-799
	void shouldNotReadSeqNoPrimaryTermProperty() {
		Document document = Document.create().append("seqNoPrimaryTerm", emptyMap());

		EntityWithSeqNoPrimaryTerm entity = mappingElasticsearchConverter.read(EntityWithSeqNoPrimaryTerm.class, document);

		assertThat(entity.seqNoPrimaryTerm).isNull();
	}

	@Test // DATAES-845
	void shouldWriteCollectionsWithNullValues() throws JSONException {
		EntityWithListProperty entity = new EntityWithListProperty();
		entity.setId("42");
		entity.setValues(Arrays.asList(null, "two", null, "four"));

		String expected = '{' + //
				"  \"id\": \"42\"," + //
				"  \"values\": [null, \"two\", null, \"four\"]" + //
				'}';
		Document document = Document.create();
		mappingElasticsearchConverter.write(entity, document);
		String json = document.toJson();

		assertEquals(expected, json, false);
	}

	private String pointTemplate(String name, Point point) {
		return String.format(Locale.ENGLISH, "\"%s\":{\"lat\":%.1f,\"lon\":%.1f}", name, point.getX(), point.getY());
	}

	private Map<String, Object> writeToMap(Object source) {

		Document sink = Document.create();
		mappingElasticsearchConverter.write(source, sink);
		return sink;
	}

	public static class Sample {

		@Nullable public @ReadOnlyProperty String readOnly;
		@Nullable public @Transient String annotatedTransientProperty;
		@Nullable public transient String javaTransientProperty;
		@Nullable public String property;
	}

	@Data
	static class Person {

		@Id String id;
		String name;
		@Field(name = "first-name") String firstName;
		@Field(name = "last-name") String lastName;
		@Field(name = "birth-date", type = FieldType.Date, format = DateFormat.custom,
				pattern = "dd.MM.uuuu") LocalDate birthDate;
		Gender gender;
		Address address;

		List<Person> coWorkers;
		List<Inventory> inventoryList;
		Map<String, Address> shippingAddresses;
		Map<String, Inventory> inventoryMap;
	}

	enum Gender {

		MAN("1"), MACHINE("0");

		String theValue;

		Gender(String theValue) {
			this.theValue = theValue;
		}

		public String getTheValue() {
			return theValue;
		}
	}

	interface Inventory {

		String getLabel();
	}

	@Getter
	@RequiredArgsConstructor
	@EqualsAndHashCode
	static class Gun implements Inventory {

		final String label;
		final int shotsPerMagazine;

		@Override
		public String getLabel() {
			return label;
		}
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	static class Grenade implements Inventory {

		final String label;

		@Override
		public String getLabel() {
			return label;
		}
	}

	@TypeAlias("rifle")
	@EqualsAndHashCode
	@RequiredArgsConstructor
	static class Rifle implements Inventory {

		final String label;
		final double weight;
		final int maxShotsPerMagazine;

		@Override
		public String getLabel() {
			return label;
		}
	}

	@EqualsAndHashCode
	@RequiredArgsConstructor
	static class ShotGun implements Inventory {

		final String label;

		@Override
		public String getLabel() {
			return label;
		}
	}

	@Data
	static class Address {

		Point location;
		String street;
		String city;
	}

	@EqualsAndHashCode(callSuper = true)
	@Data
	static class Place extends Address {

		String name;
	}

	@Data
	static class Skynet {

		Object object;
		List<Object> objectList;
		Map<String, Object> objectMap;
	}

	@Data
	static class Notification {

		Long id;
		String fromEmail;
		String toEmail;
		Map<String, Object> params;
	}

	@WritingConverter
	static class ShotGunToMapConverter implements Converter<ShotGun, Map<String, Object>> {

		@Override
		public Map<String, Object> convert(ShotGun source) {

			LinkedHashMap<String, Object> target = new LinkedHashMap<>();
			target.put("model", source.getLabel());
			target.put("_class", ShotGun.class.getName());
			return target;
		}
	}

	@ReadingConverter
	static class MapToShotGunConverter implements Converter<Map<String, Object>, ShotGun> {

		@Override
		public ShotGun convert(Map<String, Object> source) {
			return new ShotGun(source.get("model").toString());
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	static class Car {

		private String name;
		private String model;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@org.springframework.data.elasticsearch.annotations.Document(indexName = "test-index-geo-core-entity-mapper",
			replicas = 0, refreshInterval = "-1")
	static class GeoEntity {

		@Id private String id;

		// geo shape - Spring Data
		private Box box;
		private Circle circle;
		private Polygon polygon;

		// geo point - Custom implementation + Spring Data
		@GeoPointField private Point pointA;

		private GeoPoint pointB;

		@GeoPointField private String pointC;

		@GeoPointField private double[] pointD;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class SchemaLessObjectWrapper {

		private Map<String, Object> schemaLessObject;
	}

	@Data
	@org.springframework.data.elasticsearch.annotations.Document(
			indexName = "test-index-entity-with-seq-no-primary-term-mapper")
	static class EntityWithSeqNoPrimaryTerm {

		@Nullable private SeqNoPrimaryTerm seqNoPrimaryTerm;
	}

	@Data
	static class EntityWithListProperty {
		@Id private String id;

		private List<String> values;
	}
}
