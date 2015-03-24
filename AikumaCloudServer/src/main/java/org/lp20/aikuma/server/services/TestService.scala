package org.lp20.aikuma.server.services

import javax.ws.rs._
import javax.ws.rs.core._
import javax.xml.bind.annotation._

@XmlRootElement(name = "person")
@XmlAccessorType(XmlAccessType.FIELD)
case class TestBean(
  @XmlElement(required=true) fullname: String,
  age: Int) {
  def this() = this("", 0)
}

@Path("test")
class TestService {
  @GET
  @Path("func1/{p1}/{p2}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def func1(
    @PathParam("p1") p1: String,
    @PathParam("p2") p2: String
  ): TestBean = {
    new TestBean(p1, 13)
  }
}

