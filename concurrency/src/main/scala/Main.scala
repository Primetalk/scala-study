import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global


@main def hello: Unit =

    //У меня четырехпоточный, двухъядерный процессор. Потому по совету из интернета,
    // я сделал это в четырех потоках.
    
    val pathToFile = "/home/lstchk/Downloads/shakespeare-hamlet.txt"  
    
    //Функция, составляющая массив из длин слов. Сначала заменяет все, кроме букв латинского алфавита на пробелы,
    //потом, разделяет текст по пробелам и считает длину получившихся слов. Так как, в том числе считаются и пустые строки (так как замена),
    // то нужно удалить нули 
    def count(x: String): Seq[Int] = x.replaceAll("[^a-zA-Z]", " ").split(" ").map(_.length).toList.filter({ _ != 0 })
    
    //Функция заполняет коллекцию длинами слов в опреленном интервале
    def splitter(s: Int, f: Int): Seq[Int]  =   {
      var foo: Seq[Int]= Seq()
      for(x <- Source.fromFile(pathToFile).getLines.slice(s, f)) 
         foo ++= (count(x))   
      foo
    }
    
    //Функция считает нужный нам результат на части
    def average(seq: Seq[Int]): Int = seq.sum/seq.length
    
    
    val len = Source.fromFile(pathToFile).getLines.length
    
     //Собственно, многопоточность. Создание задач
    val task1: Future[Int] =  Future{average(splitter(0, len/4))}
    val task2: Future[Int] = Future{average(splitter(0, len/4))}
    val task3: Future[Int] = Future{average(splitter(len/4, len/2))}
    val task4: Future[Int] = Future{average(splitter(len/2, len-len/4))}
    
    //Выполнение задач
    val allAverage= for{
      t1<-task1
      t2<-task2
      t3<-task3
      t4<-task4
    } yield ((t1+t2+t3+t4)/4)
    
    //Вывод результата
    println(Await.result(allAverage,scala.concurrent.duration.Duration(100, "seconds")))
    
