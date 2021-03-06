package examples

import breeze.stats._
import breeze.linalg.{DenseMatrix, DenseVector}
import breeze.stats.distributions.MultivariateGaussian
import cp_detectors.LRTOfflineDetector
import datasets.{Dataset, DenseVectorDataset, MultiColumnDataset}
import models.standart.NormalModelVecMean
import patterns.{NoPattern, PatternFactory, StaticTrianglePattern, TrianglePattern}

/**
  * Created by buzun on 22/11/16.
  */
object QualityTest extends App {

  private def data_gen(r : MultivariateGaussian, dim : Int, n : Int, dm: Double): DenseVectorDataset = {
    Dataset.applyVec(
      r.sample(n / 2).toIndexedSeq ++
        r.sample(n / 2).toIndexedSeq.map(x => x + DenseVector.fill(dim)(dm)))
  }

  def exec(windowSize : Int, patternFactory: PatternFactory) = {
    val dim = 30
    val model = new NormalModelVecMean(dim)

    val r = MultivariateGaussian(DenseVector.zeros[Double](dim), DenseMatrix.eye[Double](dim))

    val dm = 0.2
    val n = 500

    val data = data_gen(r, dim, n, dm)

    val detector = new LRTOfflineDetector(model, 0.05, Array(windowSize), patternFactory)
    detector.init(data)

    detector.findOne(data)
  }

  private val iterations = 20
  val results = (1 to iterations).map(_ => exec(70, NoPattern))

  val power = results.count(_.isDefined).toDouble / iterations
  val sd = results.flatten.map(x => math.abs(x - 250))

  println("POWER " + power)
  println("SD    " + sd)
}
