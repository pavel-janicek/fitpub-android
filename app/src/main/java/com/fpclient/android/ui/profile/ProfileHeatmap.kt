package com.fpclient.android.ui.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fpclient.android.data.dto.HeatmapBoundsDto
import com.fpclient.android.data.dto.HeatmapPointDto
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme

/**
 * Activity-location heatmap rendered as a fast point overlay over an OSM base map,
 * mirroring the web app's profile heatmap. Shown only when the server returns points.
 */
@Composable
fun HeatmapCard(
    points: List<HeatmapPointDto>,
    bounds: HeatmapBoundsDto?,
    modifier: Modifier = Modifier,
) {
    val valid = points.filter { it.latitude != null && it.longitude != null }
    if (valid.isEmpty()) return
    Card(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Text(
            "Activity heatmap",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 14.dp, top = 12.dp),
        )
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                }
            },
            update = { map ->
                map.overlays.removeAll { it is SimpleFastPointOverlay }
                val geoPoints = valid.map { GeoPoint(it.latitude!!, it.longitude!!) }
                val theme = SimplePointTheme(geoPoints.toList(), false)
                val options = SimpleFastPointOverlayOptions.getDefaultStyle()
                map.overlays.add(SimpleFastPointOverlay(theme, options))
                val box = if (bounds != null &&
                    bounds.minLatitude != null && bounds.minLongitude != null &&
                    bounds.maxLatitude != null && bounds.maxLongitude != null
                ) {
                    BoundingBox(bounds.maxLatitude!!, bounds.maxLongitude!!, bounds.minLatitude!!, bounds.minLongitude!!)
                } else {
                    BoundingBox.fromGeoPoints(geoPoints)
                }
                map.post {
                    map.zoomToBoundingBox(box.increaseByScale(1.15f), false)
                }
            },
            modifier = Modifier.fillMaxWidth().height(200.dp),
        )
    }
}