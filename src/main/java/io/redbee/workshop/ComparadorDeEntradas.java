package io.redbee.workshop;

import java.util.Map;
import java.util.Comparator;

public class ComparadorDeEntradas<S,T extends Comparable<T>> implements Comparator<Map.Entry<S,T>> {
	public int compare(Map.Entry<S,T> a, Map.Entry<S,T> b) {
		return b.getValue().compareTo(a.getValue());
	}
}
