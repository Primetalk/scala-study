//@main def hello: Unit =
//    println(s"Hello world!")

import scala.io.Source
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

//Program compute average length of words in Hamlet

@main  def hello: Unit = {

    def avgStringLen (ints: List[Int]): Double={
      1.0*ints.sum/ints.length
    }

    val filePath = "hamlet.txt";  //file with data
    var intList1 = List.empty[Int]  //we divide data into two parts
    var intList2 = List.empty[Int]  //because we calculate on CPU with two core
    for  (pairs <- Source.fromFile(filePath).getLines().sliding(2,2)) {
        intList1:::= pairs.head.trim.split(" ").map(_.toString.length).toList  //we read data into two arrays
        intList2:::= pairs.last.trim.split(" ").map(_.toString.length).toList  //in order to calc it in differect streams

    }
    //let s make two furures
    val task1:Future[Double] =Future{
        avgStringLen(intList1)
      }

    val task2:Future[Double] =Future{
        avgStringLen(intList2)
    }
    val resultF= for{
      s1<-task1
      s2<-task2
    } yield (s1+s2)/2.0
    val result =Await.result(resultF,30.seconds)

    println(s"average length of words in Hamlet is: $result")
  }
}
