package com.framework.core.search.index.factory.config;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;

public class HashMapAdapter extends
		XmlAdapter<LocalHashMap<String, String>, HashMap<String, String>> {

	@Override
	public HashMap<String, String> unmarshal(LocalHashMap<String, String> v)
			throws Exception {
		HashMap hashMap = new HashMap();
		for (LocalHashMapEntry entry : v.getProperties()) {
			hashMap.put(entry.getKey(), entry.getValue());
		}

		return hashMap;
	}

	@Override
	public LocalHashMap<String, String> marshal(HashMap<String, String> v)
			throws Exception {
		LocalHashMap localHashMap = new LocalHashMap();
		for (String key : v.keySet()) {
			LocalHashMapEntry localHashMapEntry = new LocalHashMapEntry();
			localHashMapEntry.setKey(key);
			localHashMapEntry.setValue(v.get(key));
			localHashMap.getProperties().add(localHashMapEntry);
		}

		return localHashMap;
	}
}
