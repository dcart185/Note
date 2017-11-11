package utility

import java.sql.{CallableStatement, Connection, ResultSet}

import play.api.Logger

object DBUtil {

  val logger :Logger= Logger(getClass)
  val log = logger.logger

  def closeQuietlyConnection(connection:Connection)={
    try{
      if(connection != null){
        connection.close()
      }
    }
    catch {
      case ex:Exception=> log.error("unable to close connection",ex)
    }
  }

  def closeQuietlyConnectionStatement(callableStatement:CallableStatement)={
    try{
      if(callableStatement != null){
        callableStatement.close()
      }
    }
    catch {
      case ex:Exception=> log.error("unable to close callable statement",ex)
    }
  }

  def closeQuietlyResultSet(resultSet:ResultSet)={
    try{
      if(resultSet != null){
        resultSet.close()
      }
    }
    catch {
      case ex:Exception=> log.error("unable to resultset",ex)
    }
  }



}
