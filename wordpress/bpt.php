<?php
/*
Plugin Name: BackPackTrack
Plugin URI: https://github.com/M66B/BackPackTrackII
Description: BackPackTrack XML-RPC methods
Version: 0.8
Author: Marcel Bokhorst
Author URI: http://blog.bokhorst.biz/about/
*/

/*
	Copyright 2011-2015 Marcel Bokhorst

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

// https://codex.wordpress.org/XML-RPC_Extending
add_filter('xmlrpc_methods', 'bpt_xmlrpc_methods');

// https://codex.wordpress.org/Plugin_API/Filter_Reference/upload_mimes
add_filter('upload_mimes', 'bpt_upload_mimes');

function bpt_xmlrpc_methods($methods) {
	$methods['bpt.upload'] = 'bpt_upload';
	return $methods;
}

function bpt_upload($args) {
	try {
		global $wpdb;
		global $wp_xmlrpc_server;

		// Decode arguments
		$blog_ID = (int) $args[0];
		$username = $wpdb->escape($args[1]);
		$password = $wpdb->escape($args[2]);
		$data = $args[3];

		$name = sanitize_file_name($data['name']);
		$type = $data['type'];
		$bits = $data['bits'];

		logIO('O', 'bpt.upload ' . $name . ' ' . strlen($bits) . ' bytes');

		// Check credentials
		if (!$user = $wp_xmlrpc_server->login($username, $password)) {
			logIO('O', 'bpt.upload invalid login');
			return $wp_xmlrpc_server->error;
		}

		do_action('xmlrpc_call', 'metaWeblog.newMediaObject');

		// Check user capabilities
		if (!current_user_can('upload_files')) {
			logIO('O', 'bpt.upload no capability');
			return new IXR_Error(401, __('You are not allowed to upload files to this site.'));
		}

		if ($error = apply_filters('pre_upload_error', false))
			return new IXR_Error(500, $error);

		// Find post
		$attached = $wpdb->get_row(
			"SELECT ID, post_parent FROM {$wpdb->posts}" .
			" WHERE post_title = '{$name}'" .
			" AND post_type = 'attachment'");
		if (empty($attached)) {
			get_currentuserinfo();
			global $user_ID;
			$upload_dir = wp_upload_dir();
			// Create new draft post
			$post_data = array(
				'post_title' => basename($name, '.gpx'),
				'post_content' => '<a href="' . $upload_dir['url'] . '/' . $name . '">' . $name . '</a>',
				'post_status' => 'draft',
				'post_author' => $user_ID
			);
			$post_ID = wp_insert_post($post_data);
			logIO('O', 'bpt.upload post=' . $post_ID);
		}
		else {
			$post_ID = $attached->post_parent;
			wp_delete_attachment($attached->ID);
			logIO('O', 'bpt.upload deleted attachment id=' . $attached->ID . ' post=' . $post_ID);
		}

		// Save file
		$upload = wp_upload_bits($name, NULL, $bits);
		if (!empty($upload['error'])) {
			$error = sprintf(__('Could not write file %1$s (%2$s)'), $name, $upload['error']);
			logIO('O', 'bpt.upload ' . $error);
			return new IXR_Error(500, $error);
		}

		// Attach file
		$attachment = array(
			'post_title' => $name,
			'post_content' => '',
			'post_type' => 'attachment',
			'post_parent' => $post_ID,
			'post_mime_type' => $type,
			'guid' => $upload['url']
		);
		$id = wp_insert_attachment($attachment, $upload['file'], $post_ID);
		wp_update_attachment_metadata($id, wp_generate_attachment_metadata($id, $upload['file']));

		logIO('O', 'bpt.upload attachment=' . $id);

		// Handle upload
		return apply_filters('wp_handle_upload', array('file' => $name, 'url' => $upload['url'], 'type' => $type), 'upload');
	}
	catch (Exception $e) {
		// What?
		logIO('O', 'bpt.upload exception' . $e->getMessage());
		return new IXR_Error(500, $e->getMessage());
	}
}

function bpt_upload_mimes($mimes) {
	$mimes['gpx'] = 'text/xml';
	return $mimes;
}

?>
