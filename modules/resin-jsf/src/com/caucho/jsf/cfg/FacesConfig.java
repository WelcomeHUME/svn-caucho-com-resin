/*
 * Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.jsf.cfg;

import java.util.*;

import javax.annotation.*;

import javax.el.*;

import javax.faces.*;
import javax.faces.application.*;
import javax.faces.component.*;
import javax.faces.component.html.*;
import javax.faces.context.*;
import javax.faces.convert.*;
import javax.faces.el.*;
import javax.faces.event.*;
import javax.faces.validator.*;

import javax.xml.bind.annotation.*;

import com.caucho.config.*;
import com.caucho.jsf.application.*;
import com.caucho.util.*;

public class FacesConfig
{
  private String _id;

  private String _version;

  private ApplicationConfig _application;

  private FactoryConfig _factory;

  private ArrayList<ManagedBeanConfig> _managedBeanList
    = new ArrayList<ManagedBeanConfig>();

  private NavigationHandlerImpl _navigation
    = new NavigationHandlerImpl();

  public void setId(String id)
  {
  }

  @XmlAttribute(name="schemaLocation")
  public void setSchemaLocation(String location)
  {
  }
  
  public String getSchemaLocation()
  {
    return null;
  }

  public void setVersion(String version)
  {
  }

  @XmlElement(name="faces-config-extension")
  public void setFacesConfigExtension(BuilderProgram program)
  {
  }

  public void addManagedBean(ManagedBeanConfig managedBean)
  {
    _managedBeanList.add(managedBean);
  }
  
  public ArrayList<ManagedBeanConfig> getManagedBeans()
  {
    return _managedBeanList;
  }

  public ApplicationConfig getApplication()
  {
    return _application;
  }

  public void setApplication(ApplicationConfig app)
  {
    _application = app;
  }

  public void setFactory(FactoryConfig factory)
  {
    _factory = factory;
  }

  public void addNavigationRule(NavigationRule rule)
  {
    _navigation.addRule(rule);
  }

  public NavigationHandler getNavigationHandler()
  {
    return _navigation;
  }

  @PostConstruct
  public void init()
  {
    if (_factory != null)
      _factory.init();
  }
}
