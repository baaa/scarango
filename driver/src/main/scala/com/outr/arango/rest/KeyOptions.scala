package com.outr.arango.rest

case class KeyOptions(allowUserKeys: Option[Boolean] = None,
                      `type`: Option[String] = None,
                      increment: Option[Int] = None,
                      offset: Option[Int] = None)
