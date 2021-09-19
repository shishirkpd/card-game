package com.skp.game.model

sealed trait UserStatus

case object LOBBY extends UserStatus
case object WAITING extends UserStatus
case object PLAYING extends UserStatus