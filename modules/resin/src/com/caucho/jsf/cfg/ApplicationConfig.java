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
import java.util.logging.*;
import java.lang.reflect.*;

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
import com.caucho.util.*;

public class ApplicationConfig
{
  private static final Logger log
    = Logger.getLogger(ApplicationConfig.class.getName());
  private static final L10N L = new L10N(ApplicationConfig.class);
  
  private String _id;

  private Class _actionListener;

  private String _defaultRenderKitId;

  @XmlElement(name="message-bundle")
  private String _messageBundle;

  private Class _navigationHandler;
  
  private Class _viewHandler;
  
  private Class _stateManager;
  
  private ArrayList<ELResolver> _elResolverList
    = new ArrayList<ELResolver>();
  
  private Class _propertyResolver;
  
  private Class _variableResolver;

  private ArrayList<ResourceBundleConfig> _resourceBundleList
    = new ArrayList<ResourceBundleConfig>();

  private LocaleConfig _localeConfig;

  public void setId(String id)
  {
    _id = id;
  }

  public void setDefaultRenderKitId(String id)
  {
    _defaultRenderKitId = id;
  }

  public void setMessageBundle(String messageBundle)
  {
    _messageBundle = messageBundle;
  }

  public void setActionListener(Class actionListener)
    throws ConfigException
  {
    Config.validate(actionListener, ActionListener.class);

    _actionListener = actionListener;
  }

  public void setNavigationHandler(Class navigationHandler)
    throws ConfigException
  {
    Config.validate(navigationHandler, NavigationHandler.class);
    
    _navigationHandler = navigationHandler;
  }

  public void setViewHandler(Class viewHandler)
    throws ConfigException
  {
    if (! ViewHandler.class.isAssignableFrom(viewHandler))
      throw new ConfigException(L.l("view-handler '{0}' must extend javax.faces.application.ViewHandler.",
                                    viewHandler.getName()));

    Constructor ctor = null;

    try {
      ctor = viewHandler.getConstructor(new Class[] { ViewHandler.class });
    } catch (Exception e) {
      log.log(Level.FINEST, e.toString(), e);
    }

    try {
      if (ctor == null)
        ctor = viewHandler.getConstructor(new Class[] { });
    } catch (Exception e) {
      log.log(Level.FINEST, e.toString(), e);
    }

    if (ctor == null)
      throw new ConfigException(L.l("view-handler '{0}' must have either a zero-arg constructor or a constructor with a single ViewHandler argument.",
                                    viewHandler.getName()));
    
    _viewHandler = viewHandler;
  }

  private Class getViewHandler()
  {
    return _viewHandler;
  }

  public void setStateManager(Class stateManager)
    throws ConfigException
  {
    Config.validate(stateManager, StateManager.class);
    
    _stateManager = stateManager;
  }

  public Class getStateManager()
  {
    return _stateManager;
  }

  public void setElResolver(Class elResolver)
    throws ConfigException
  {
    Config.validate(elResolver, ELResolver.class);

    try {
      _elResolverList.add((ELResolver) elResolver.newInstance());
    } catch (Exception e) {
      throw new ConfigException(e);
    }
  }

  public Class getElResolver()
  {
    return null;
  }

  public ArrayList<ELResolver> getElResolverList()
  {
    return _elResolverList;
  }

  public void setPropertyResolver(Class propertyResolver)
    throws ConfigException
  {
    Config.validate(propertyResolver, PropertyResolver.class);
    
    _propertyResolver = propertyResolver;
  }

  public Class getPropertyResolver()
  {
    return _propertyResolver;
  }

  public void setVariableResolver(Class variableResolver)
    throws ConfigException
  {
    Config.validate(variableResolver, VariableResolver.class);
    
    _variableResolver = variableResolver;
  }

  public Class getVariableResolver()
  {
    return _variableResolver;
  }

  public void setResourceBundle(ResourceBundleConfig bundle)
    throws ConfigException
  {
    _resourceBundleList.add(bundle);
  }

  private ResourceBundleConfig getResourceBundle()
    throws ConfigException
  {
    return null;
  }

  public ArrayList<ResourceBundleConfig> getResourceBundleList()
  {
    return _resourceBundleList;
  }

  public void setApplicationExtension(BuilderProgram program)
  {
  }

  public void setLocaleConfig(LocaleConfig config)
  {
    _localeConfig = config;
  }

  public void configure(Application app)
  {
    if (_localeConfig != null)
      _localeConfig.configure(app);

    if (_viewHandler != null) {
      ViewHandler handler = null;
      
      try {
        Constructor ctor
          = _viewHandler.getConstructor(new Class[] { ViewHandler.class });
        
        ViewHandler oldHandler = app.getViewHandler();

        handler = (ViewHandler) ctor.newInstance(oldHandler);
      } catch (NoSuchMethodException e) {
      } catch (RuntimeException e) {
        throw e;
      } catch (InvocationTargetException e) {
        throw new ConfigException(e.getCause());
      } catch (Exception e) {
        throw new ConfigException(e);
      }

      if (handler == null) {
        try {
          handler = (ViewHandler) _viewHandler.newInstance();
        } catch (RuntimeException e) {
          throw e;
        } catch (Exception e) {
          throw new ConfigException(e);
        }
      }

      if (handler != null)
        app.setViewHandler(handler);
    }

    for (int i = 0; i < _elResolverList.size(); i++)
      app.addELResolver(_elResolverList.get(i));
  }

  public static class LocaleConfig {
    private Locale _defaultLocale;
    private ArrayList<Locale> _supportedLocales
      = new ArrayList<Locale>();
    
    public void setId(String id)
    {
    }

    public void setDefaultLocale(String locale)
    {
      _defaultLocale = LocaleUtil.createLocale(locale);
    }

    public void addSupportedLocale(String locale)
    {
      _supportedLocales.add(LocaleUtil.createLocale(locale));
    }

    public void configure(Application app)
    {
      if (_defaultLocale != null) {
	app.setDefaultLocale(_defaultLocale);
      }

      app.setSupportedLocales(_supportedLocales);
    }
  }
}
