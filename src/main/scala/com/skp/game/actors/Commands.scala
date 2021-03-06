package com.skp.game.actors

import akka.actor.typed.ActorRef
import com.skp.game.model.{ActionPerformed, GameType, User, UserResponse}

sealed trait Command


final case class CreateUser(user: User, replyTo: ActorRef[ActionPerformed]) extends Command

final case class GetUser(name: String, replyTo: ActorRef[UserResponse]) extends Command

final case class Play(name: String, replyTo: ActorRef[ActionPerformed], gameType: GameType.Value) extends Command

final case class StartGame(user: User, gameType: GameType.Value) extends Command

final case class BeginGame(user1: User, user2: User, gameType: GameType.Value) extends Command

final case class FoldGame(user: String, replyTo: ActorRef[ActionPerformed], gameType: GameType.Value) extends Command

final case class FoldForUser(user: User, replyTo: ActorRef[ActionPerformed], gameType: GameType.Value) extends Command

final case class FoldInProgressGameForUser(user: User, replyTo: ActorRef[ActionPerformed], gameType: GameType.Value) extends Command

final case class Show(name: String, replyTo: ActorRef[ActionPerformed], gameType: GameType.Value) extends Command

final case class ShowForUser(user: User, replyTo: ActorRef[ActionPerformed], gameType: GameType.Value) extends Command

final case class ShowInProgressGameForUser(user: User, replyTo: ActorRef[ActionPerformed], gameType: GameType.Value) extends Command