package io.redbee.workshop;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class JavaCollections {

	private static List<Movie4Java> movies;
	
	@BeforeClass
	static public void init() {
		File dsf = new File("/Volumes/Macintosh HD/Users/martinpaoletta/repositorio/redbee/proyectos/workshop/data/movies.txt");
		movies = new DatasetLoader(dsf).loadMoviesForJava();
	}
	
	@Test
	public void pruebaOptional() {

		Actor actor = new Actor("Darin, Ricardo");
		
		Optional<Movie4Java> found = 
				movies.stream()
					.filter(m -> m.getCast().contains(actor))
					.findFirst();
		
		System.out.println(found.map(Movie4Java::getTitle).orElse("No encontramos peliculas con " + actor.getName()));
		
	}
	
	
	@Test
	public void ordenAlfabetico() {
		
		System.out.println("Peliculas en orden alfabetico");
		
		movies.stream()
				.map(Movie4Java::getTitle)
				.sorted()
				.limit(100)
				.forEach(System.out::println);
		
		System.out.println("---------------------------------------");

		movies.stream()
				.sorted((m, o) -> m.getTitle()
				.compareTo(o.getTitle()))
				.limit(100)
				.forEach(System.out::println);
				 
		
	}
	
	
	public Map<Actor, List<Movie4Java>> peliculasXActor() {
		Collector<Movie4Java, Map<Actor, List<Movie4Java>>, Map<Actor, List<Movie4Java>>> 
		colectorPelis = 
			new Collector<Movie4Java, Map<Actor, List<Movie4Java>>, Map<Actor, List<Movie4Java>>>() {

				@Override
				public Supplier<Map<Actor, List<Movie4Java>>> supplier() {
					return HashMap<Actor, List<Movie4Java>>::new;
				}
	
				@Override
				public BiConsumer<Map<Actor, List<Movie4Java>>, Movie4Java> accumulator() {
					return (map, movie) -> {
						movie.getCast().stream().forEach(actor -> {
							List<Movie4Java> pelis = map.getOrDefault(actor, new ArrayList<Movie4Java>());
							pelis.add(movie);
							map.put(actor, pelis);
						});
					};
				}
	
				@Override
				public BinaryOperator<Map<Actor, List<Movie4Java>>> combiner() {
					// x ahora no
					return null;
				}
	
				@Override
				public Function<Map<Actor, List<Movie4Java>>, Map<Actor, List<Movie4Java>>> finisher() {
					return mapa -> mapa;
				}
	
				@Override
				public Set<java.util.stream.Collector.Characteristics> characteristics() {
					return EnumSet.of(Characteristics.UNORDERED);
				}
				
			};
	
	
			return movies.stream().collect(colectorPelis);		
	}
	
	
	@Test
	public void actoresMasProlificos() {
		
		System.out.println("Actores mas prolificos");
		
		Map<Actor, List<Movie4Java>> peliculasXActor = peliculasXActor();

		Map<Actor, Integer> cantPelisXActor = 
				peliculasXActor.entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().size()));
		
		List<Map.Entry<Actor, Integer>> listaCantPelisXActor = new ArrayList<Map.Entry<Actor, Integer>>();
		listaCantPelisXActor.addAll(cantPelisXActor.entrySet());
		listaCantPelisXActor.sort(new ComparadorDeEntradas<Actor, Integer>());
		
		listaCantPelisXActor.stream().limit(100).map(kv -> kv.getKey().getName() + ": " + kv.getValue()).forEach(System.out::println);
		
	}	
	
	
	@Test
	public void unoImperativo() {
		
		System.out.println("Ranking de peliculas por año. Java Imperativo");
		
		Map<Integer, Integer> pelisXAnio = new HashMap<Integer, Integer>();
		for(Movie4Java movie : movies) {
			pelisXAnio.put(movie.getYear(), pelisXAnio.getOrDefault(movie.getYear(), 0) + 1);
		}
		
		List<Map.Entry<Integer, Integer>> listaAnioNroPelis = new ArrayList<Map.Entry<Integer, Integer>>();
		listaAnioNroPelis.addAll(pelisXAnio.entrySet());
		Comparator<Map.Entry<Integer, Integer>> cantEntryComp = new Comparator<Map.Entry<Integer,Integer>>() {
			public int compare(Map.Entry<Integer,Integer> a, Map.Entry<Integer,Integer> b) {
				return b.getValue().compareTo(a.getValue());
			}
		};
		listaAnioNroPelis.sort(cantEntryComp);
		
		for (Map.Entry<Integer, Integer> kv : listaAnioNroPelis) {
			System.out.printf("%d: %d\n", kv.getKey(), kv.getValue());
		}
		
	} 
	
	@Test
	public void unoConStreams() {
		
		System.out.println("Ranking de peliculas por año. Java Streams");
		
		Map<Integer, Integer> cantPelisXAnio = 
				movies.stream()
				.collect(Collectors.groupingBy(Movie4Java::getYear))
				.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().size()));
		
		cantPelisXAnio.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).
			forEach(kv -> System.out.printf("%d: %d\n", kv.getKey(), kv.getValue()));;
		
	}
	
	@Test
	public void dosImperativo() {
		System.out.println("Peliculas con Kevin Bacon. Java Imperativo");
		Actor kb = new Actor("Bacon, Kevin");
		for(Movie4Java movie : movies) {
			if(movie.getCast().contains(kb))
				System.out.println(movie);
		}
		
	}
	
	@Test
	public void dosFuncional() {
		
		System.out.println("Peliculas con Kevin Bacon. Java con streams");
		
		movies.stream()
				.filter(m -> m.getCast().contains(new Actor("Bacon, Kevin")))
				.forEach(System.out::println);
		
	}
	
	@Test
	public void algunasMetricas() {
		
		System.out.println("Peliculas " + movies.size());

		movies.stream().map(Movie4Java::getCast).reduce(new HashSet<Actor>(), new BinaryOperator<Set<Actor>>() {
			public Set<Actor> apply(Set<Actor> a, Set<Actor> b) {
				a.addAll(b);
				return a;
			}
		});

		Set<Actor> todosLosActores = movies.stream().map(Movie4Java::getCast).reduce(new HashSet<Actor>(), (s1, s2) -> {
			s1.addAll(s2);
			return s1;
		});
		
		
		System.out.println("Universo de actores: " + todosLosActores.size());
		
		Set<Actor> todosLosActores2 = movies.stream().flatMap(m -> m.getCast().stream()).collect(Collectors.toSet());
		assertEquals(todosLosActores.size(), todosLosActores2.size());
		
	}

	

	
//	static class comparadorDeEntradas<Map.Entry<S,T>> extends Comparator<Map.Entry<Integer,Integer>> {
//		public int compare(Map.Entry<S,T> a, Map.Entry<S,T> b) {
//			return b.getValue().compareTo(a.getValue());
//		}
//	};
	
	static public class ComparadorDeEntradas<S,T extends Comparable<T>> implements Comparator<Map.Entry<S,T>> {
		public int compare(Map.Entry<S,T> a, Map.Entry<S,T> b) {
			return b.getValue().compareTo(a.getValue());
		}
	}	
	
	
}
