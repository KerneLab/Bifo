-- phpMyAdmin SQL Dump
-- version 2.11.4
-- http://www.phpmyadmin.net
--
-- 主机: localhost
-- 生成日期: 2010 年 07 月 26 日 22:56
-- 服务器版本: 5.0.45
-- PHP 版本: 5.1.4

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- 数据库: `bifo`
--

-- --------------------------------------------------------

--
-- 表的结构 `gene2go`
--

DROP TABLE IF EXISTS `gene2go`;
CREATE TABLE IF NOT EXISTS `gene2go` (
  `go` varchar(7) NOT NULL,
  `category` varchar(9) NOT NULL,
  `term` text NOT NULL,
  PRIMARY KEY  USING BTREE (`go`),
  KEY `Index_cat` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `gene2go_full`
--

DROP TABLE IF EXISTS `gene2go_full`;
CREATE TABLE IF NOT EXISTS `gene2go_full` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `go` varchar(7) NOT NULL,
  `category` varchar(9) NOT NULL,
  `term` text NOT NULL,
  `gene` int(10) unsigned NOT NULL,
  `tax` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `Index_go` (`go`),
  KEY `Index_cat` (`category`),
  KEY `Index_gen` (`gene`),
  KEY `Index_tax` (`tax`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `interact`
--

DROP TABLE IF EXISTS `interact`;
CREATE TABLE IF NOT EXISTS `interact` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `receptor` varchar(6) NOT NULL,
  `ligand` varchar(6) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `Index_rec` (`receptor`),
  KEY `Index_lig` (`ligand`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `interactgo`
--

DROP TABLE IF EXISTS `interactgo`;
CREATE TABLE IF NOT EXISTS `interactgo` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `species` varchar(8) NOT NULL,
  `rgo` varchar(7) NOT NULL,
  `lgo` varchar(7) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `Index_spe` (`species`),
  KEY `Index_rgo` (`rgo`),
  KEY `Index_lgo` (`lgo`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `interactgo_solely`
--

DROP TABLE IF EXISTS `interactgo_solely`;
CREATE TABLE IF NOT EXISTS `interactgo_solely` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `rgo` varchar(7) NOT NULL,
  `lgo` varchar(7) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `Index_rgo` USING BTREE (`rgo`),
  KEY `Index_lgo` USING BTREE (`lgo`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `uniprotgo`
--

DROP TABLE IF EXISTS `uniprotgo`;
CREATE TABLE IF NOT EXISTS `uniprotgo` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `uniprot` varchar(6) NOT NULL,
  `go` varchar(7) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `Index_uni_go` (`uniprot`,`go`),
  KEY `Index_uni` (`uniprot`),
  KEY `Index_go` (`go`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `uniprotkb`
--

DROP TABLE IF EXISTS `uniprotkb`;
CREATE TABLE IF NOT EXISTS `uniprotkb` (
  `id` varchar(6) NOT NULL,
  `species` varchar(8) NOT NULL,
  `entry` varchar(8) NOT NULL,
  `detail` text NOT NULL,
  `code` text NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `Index_spe` (`species`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
