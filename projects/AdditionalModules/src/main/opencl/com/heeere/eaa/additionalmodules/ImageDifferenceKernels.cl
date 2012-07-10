
__kernel void
image_difference(
        __write_only image2d_t out,
        __read_only image2d_t image1,
        __read_only image2d_t image2)
{
    sampler_t sampler = CLK_FILTER_NEAREST|CLK_NORMALIZED_COORDS_FALSE|CLK_ADDRESS_CLAMP_TO_EDGE;
    
    int2 coord = (int2)(get_global_id(0), get_global_id(1));

    float4 v1 = read_imagef(image1, sampler, coord);
    float4 v2 = read_imagef(image2, sampler, coord);
    float4 d = v2 - v1;
    
    write_imagef(out, coord, d);
}

__kernel void
image_difference_absolute(
        __write_only image2d_t out,
        __read_only image2d_t image1,
        __read_only image2d_t image2)
{
    const sampler_t sampler = CLK_FILTER_NEAREST|CLK_NORMALIZED_COORDS_FALSE|CLK_ADDRESS_CLAMP_TO_EDGE;
    
    int2 coord = (int2)(get_global_id(0), get_global_id(1));

    float4 v1 = read_imagef(image1, sampler, coord);
    float4 v2 = read_imagef(image2, sampler, coord);
    float d = distance(v2, v1);
    
    write_imagef(out, coord, d);
}

