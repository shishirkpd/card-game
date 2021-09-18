package com.skp.game.model

sealed trait StatusEnum

case object LOBBY extends StatusEnum
case object WAITING extends StatusEnum
case object PLAYING extends StatusEnum