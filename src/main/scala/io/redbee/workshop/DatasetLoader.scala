package io.redbee.workshop

import scala.collection.JavaConverters._
import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import java.io.File
import scala.io.Source
import com.google.common.base.Splitter
import scala.util.Try
import scala.beans.BeanProperty
import org.junit.Test
import org.junit.Assert._


class ScalaCollectionsExamples {
  
  val movies = 
    new DatasetLoader(new File("/Volumes/Macintosh HD/Users/martinpaoletta/repositorio/redbee/proyectos/workshop/data/movies.txt")).
    safeLoad
  
  
  def trabajoCon(actor: Actor) = peliculasCon(actor).foldLeft(Set[Actor]())((l, m) => l ++ (m.cast - actor))
  
  @Test
  def algunasMetricas = {
    
    println("Peliculas " + movies.size);
        
    val todosLosActores = movies.map(_.cast).foldLeft(Set[Actor]())((s, cast) => s ++ cast)
    val todosLosActores2 = movies.flatMap(_.cast).toSet
    assertEquals(todosLosActores.size, todosLosActores2.size)
    println("Universo de actores: " + todosLosActores.size)
    
  }
  
  
  
  
//  @Test
  def tres = {
    
    lazy val primerGrado = trabajoCon(kb)
    lazy val segundoGrado = primerGrado flatMap { trabajoCon }
    lazy val tercerGrado = segundoGrado flatMap { trabajoCon }
    lazy val cuartoGrado = tercerGrado flatMap { trabajoCon }
    lazy val quintoGrado = cuartoGrado flatMap { trabajoCon }
    lazy val sextoGrado = quintoGrado flatMap { trabajoCon }

    def gradosDeSeparacionCon(actor: Actor): Stream[Set[Actor]] = gradosDeSeparacion(Set(actor))
    
    def gradosDeSeparacion(actores: Set[Actor]): Stream[Set[Actor]] = Stream.cons(actores, gradosDeSeparacion(actores flatMap { trabajoCon }))
    
    val gskb = gradosDeSeparacionCon(kb)
    
    println(primerGrado)
    
    println(gskb(0))
    
    assertEquals(gskb(0), Set(kb))
        
    assertEquals(gskb(1), primerGrado)

    println(segundoGrado.size)
    println(gskb(2).size)
    
    assertEquals(gskb(2), segundoGrado)
    
    assertEquals(gskb(3), tercerGrado)
    
    assertEquals(gskb(4), cuartoGrado)
    
    assertEquals(gskb(5), quintoGrado)
    
    assertEquals(gskb(6), sextoGrado)
    
  }
  
  
  lazy val peliculasXActor = movies.foldLeft(Map[Actor, List[Movie]]())((pelisXActor, movie) => {
    movie.cast.foldLeft(pelisXActor) ((mapa, a) => {
      val pelis = mapa.getOrElse(a, List[Movie]())
    	mapa + (a -> (movie :: pelis))
    })})
  

  def peliculasCon(actor: Actor) = movies.filter(_.cast.contains(actor))
  
  val kb = Actor("Bacon, Kevin")
  
  @Test
  def dos = {

    println("Peliculas con Kevin Bacon")
    
    // Peliculas en las que trabajo Kevin Bacon
    peliculasCon(kb).foreach(println)
    
    
    println("Peliculas con Ron Jeremy")
    val rj = Actor("Jeremy, Ron")
    peliculasXActor(rj).foreach(println)
    assertEquals(peliculasCon(rj), peliculasXActor(rj))
    
  }
  
  @Test
  def uno = {
    // 1) Peliculas por año
    println("Ranking de peliculas por año")
  
    movies.
        foldLeft(Map[Int, List[Movie]]())((map, movie) => map + (movie.getYear() -> (movie :: map.getOrElse(movie.getYear(), List[Movie]())))).
        map(ylm => ylm._1 -> ylm._2.size).
        toList.
        sortBy(_._2).reverse.
        foreach(println)
  
    // o, usando groupBy
    movies.groupBy(_.year).map(ylm => ylm._1 -> ylm._2.size).toList.sortBy(_._2).reverse.foreach(println)    
  }
  
  
  
  
}



/**
 * @author martinpaoletta
 */
class DatasetLoader(file: File) {
  
  def load(): List[Try[Movie]] = {

    val dashSplitter = Splitter.on('/')
    val parenthesisSplitter = Splitter.on('(')
    val parenthesisMatcher = CharMatcher.anyOf("()")
    
    val movies = Source.fromFile(file).getLines().map(line => {
      Try {
    	  val parts = dashSplitter.split(line).asScala
			  val movieAndYear = parts.head
			  val title = movieAndYear.take(if(movieAndYear.lastIndexOf('(') > 0) movieAndYear.lastIndexOf('(') else movieAndYear.length()).trim
			  val year = parenthesisMatcher.removeFrom(parenthesisSplitter.split(movieAndYear).asScala.last).toInt
			  val cast = parts.tail.map(Actor(_)).toSet
			  Movie(title, year, cast)      
      }
    })
    
    val movieStream = movies.toStream
    
    movies.toList
  }

  def safeLoad() = {
    
    val dataset = load
    
    val (success, failed) = dataset.partition { _.isSuccess }
      
    println("Tamaño de dataset: (total: %d / ok: %d / error: %d)".format(dataset.size, success.size, failed.size))
    
    success.map(_.get)    
  }
  
  def loadMoviesForJava() = {
    
    var javalist = new java.util.ArrayList[Movie4Java]()
    javalist.addAll(safeLoad.map(movie => {
      var cast4java = new java.util.HashSet[Actor]();
      cast4java.addAll(movie.cast.asJava)
    	Movie4Java(movie.title, movie.year, cast4java)
    }).asJava)
    javalist
    
  }
  
  
}

case class Movie(@BeanProperty title: String, @BeanProperty year: Int, @BeanProperty cast: Set[Actor])
case class Actor(@BeanProperty name: String)

case class Movie4Java(@BeanProperty title: String, @BeanProperty year: Int, @BeanProperty cast: java.util.Set[Actor])