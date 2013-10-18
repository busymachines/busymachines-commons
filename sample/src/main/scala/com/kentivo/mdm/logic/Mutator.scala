package com.kentivo.mdm.logic

import scala.language.postfixOps
import com.kentivo.mdm.domain.Mutation
import com.kentivo.mdm.domain.Item
import com.busymachines.commons
import com.busymachines.commons.implicits._
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.PropertyScope
import java.util.Locale
import scala.collection.concurrent.TrieMap
import com.kentivo.mdm.domain.PropertyValue
import com.busymachines.commons.domain.UnitOfMeasure
import com.kentivo.mdm.db.ItemDao
import scala.concurrent.ExecutionContext
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.SearchCriteria
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import com.busymachines.commons.dao.RootDaoMutator
import com.kentivo.mdm.domain.Repository
import com.busymachines.commons.dao.Page
import com.kentivo.mdm.domain.Channel
import com.kentivo.mdm.domain.ItemDefinition

object Commands {
  
  /**
   * When value == None, removes the value.
   */
  case class SetValue(item : Id[Item], property : Id[Property], value : Option[String], channel : Option[Channel] = None, locale : Option[Locale] = None)

  case class SetItemDefinition(item : Id[Item], definition: ItemDefinition)
}

class Mutator(val itemDao : ItemDao, val repository : Repository, val mutation: Mutation)(implicit ec: ExecutionContext) {

  private val mutator = new RootDaoMutator[Item](itemDao)
  private val timeout = 1 minute
  
  private def createItem(id : Id[Item]) = Item(id, repository.id, mutation.id)

  def createItem = Item(Id.generate, repository.id, mutation.id)
  
  def retrieve(id : Id[Item]) = 
    mutator.retrieve(id, timeout)

  def retrieve(ids : Seq[Id[Item]]) = 
    mutator.retrieve(ids, timeout)

  def searchItems(criteria : SearchCriteria[Item], page : Page, timeout : Duration) : Seq[Item] = 
    mutator.search(criteria, page, timeout)
    
  def getChangedItem(id: Id[Item]): Option[Item] =
    mutator.changedEntities.get(id)

  def getOrCreateItem(id: Id[Item]): Item = 
    mutator.getOrCreate(id, createItem(id), timeout)

  def modifyItem(itemId: Id[Item])(modify: Item => Item): Item = 
    mutator.modify(itemId, createItem(itemId), timeout)(modify)
  
  def modifyProperty(itemId: Id[Item], propertyId: Id[Property])(modify: Property => Property): Property = {
    val item = getOrCreateItem(itemId)
    val (properties, property, changed) = sk.modify(_.id == propertyId, Property(propertyId, mutation.id), modify)
    if (changed) {
      mutator.update(item.copy(properties = properties), timeout)
    }
    property
  }

  def modifyValues(itemId: Id[Item], propertyId: Id[Property])(modify: List[PropertyValue] => List[PropertyValue]) : Item = {
    val item = getOrCreateItem(itemId)
    val (values, otherValues) = item.values.partition(_.property == propertyId)
    val newValues = otherValues ++ modify(values) 
    if (item.values != newValues) {
      val modItem = item.copy(values = newValues)
      mutator.update(item.copy(values = newValues), timeout)
      modItem
    } else {
      item
    }
  }
  
  def setValues(itemId: Id[Item], propertyId: Id[Property], values: Seq[(Locale, String)]) : Item =
    modifyValues(itemId, propertyId)(_ => values.toList.map { 
      case (locale, value) => PropertyValue(propertyId, mutation.id, value, Some(locale))
    })
  
  def setValue(itemId: Id[Item], propertyId: Id[Property], value : Option[String], locale : Option[Locale] = None, unit : Option[Unit] = None) : Item = {
    val item = getOrCreateItem(itemId)
    val (newValues, _, changed) = value match {
      case Some(value) => 
        item.values.modify(_.locale == locale, PropertyValue(propertyId, mutation.id, value, locale, unit))
      case None =>
        val newValues = item.values.filterNot(_.locale == locale)
        (newValues, Unit, item.values != newValues)
    }
    if (changed) {
      val modItem = item.copy(values = newValues)
      mutator.update(item.copy(values = newValues), timeout)
      modItem
    }
    else item
  }
  
  def write(timeout : Duration) {
    mutator.write(timeout, true);
  }
}
